import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import TrackPlayer, {
  State,
  RepeatMode as RNTPRepeatMode,
  usePlaybackState,
  useProgress,
  useActiveTrack,
} from 'react-native-track-player';
import { Track, RepeatMode, PlaybackState, QueueState } from '../types';

export interface MusicPlayerActions {
  play: () => void;
  pause: () => void;
  togglePlayPause: () => void;
  next: () => void;
  previous: () => void;
  seekTo: (seconds: number) => void;
  setQueue: (tracks: Track[], startIndex?: number) => void;
  addToQueue: (tracks: Track[]) => void;
  selectTrack: (index: number) => void;
  toggleShuffle: () => void;
  cycleRepeatMode: () => void;
}

export interface MusicPlayerState {
  playback: PlaybackState;
  queue: QueueState;
  currentTrack: Track | null;
}

export type MusicPlayer = MusicPlayerState & MusicPlayerActions;

function shuffleArray<T>(array: T[]): T[] {
  const shuffled = [...array];
  for (let i = shuffled.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
  }
  return shuffled;
}

function toRNTPTrack(track: Track) {
  return {
    id: track.id,
    url: track.uri,
    title: track.title,
    artist: track.artist,
    album: track.album,
    artwork: track.artwork ?? undefined,
    duration: track.duration,
  };
}

function toRNTPRepeatMode(mode: RepeatMode): RNTPRepeatMode {
  switch (mode) {
    case 'off':
      return RNTPRepeatMode.Off;
    case 'all':
      return RNTPRepeatMode.Queue;
    case 'one':
      return RNTPRepeatMode.Track;
  }
}

export function useMusicPlayer(): MusicPlayer {
  const [queue, setQueueState] = useState<QueueState>({
    tracks: [],
    currentIndex: -1,
    shuffleEnabled: false,
    repeatMode: 'off',
  });
  const [optimisticIsPlaying, setOptimisticIsPlaying] = useState<boolean | null>(null);

  const originalOrderRef = useRef<Track[]>([]);
  const isUpdatingQueue = useRef(false);

  // RNTP hooks
  const { state: playbackState } = usePlaybackState();
  const { position, duration } = useProgress(250);
  const activeTrack = useActiveTrack();

  const currentTrack = useMemo(
    () =>
      queue.currentIndex >= 0 && queue.currentIndex < queue.tracks.length
        ? queue.tracks[queue.currentIndex]
        : null,
    [queue.currentIndex, queue.tracks]
  );

  // Keep RNTP repeat mode in sync
  useEffect(() => {
    TrackPlayer.setRepeatMode(toRNTPRepeatMode(queue.repeatMode));
  }, [queue.repeatMode]);

  // Sync currentIndex when RNTP auto-advances (e.g., track ends)
  useEffect(() => {
    if (!activeTrack || isUpdatingQueue.current) return;
    const rntpIndex = queue.tracks.findIndex((t) => t.id === activeTrack.id);
    if (rntpIndex >= 0 && rntpIndex !== queue.currentIndex) {
      setQueueState((prev) => ({ ...prev, currentIndex: rntpIndex }));
    }
  }, [activeTrack, queue.tracks, queue.currentIndex]);

  // Hydrate state from native player on mount
  useEffect(() => {
    async function hydrate() {
      try {
        const currentQueue = await TrackPlayer.getQueue();
        const currentIndex = await TrackPlayer.getActiveTrackIndex();

        if (currentQueue.length > 0) {
          const mappedTracks: Track[] = currentQueue.map((t) => ({
            id: t.id || 'unknown',
            uri: typeof t.url === 'string' ? t.url : '',
            title: t.title || 'Unknown',
            artist: t.artist || 'Unknown',
            album: t.album || 'Unknown',
            artwork: typeof t.artwork === 'string' ? t.artwork : null,
            duration: t.duration || 0,
          }));

          setQueueState((prev) => ({
            ...prev,
            tracks: mappedTracks,
            currentIndex: currentIndex ?? 0,
          }));
        }
      } catch (error) {
        console.warn('Failed to hydrate player state:', error);
      }
    }

    hydrate();
  }, []);

  // Derived playback state
  const actualIsPlaying = playbackState === State.Playing;
  const isPlaying = optimisticIsPlaying !== null ? optimisticIsPlaying : actualIsPlaying;
  const isBuffering =
    playbackState === State.Buffering || playbackState === State.Loading;
  const isLoaded =
    playbackState !== undefined &&
    playbackState !== State.None &&
    playbackState !== State.Error;

  // Reset optimistic state when actual state catches up
  useEffect(() => {
    if (optimisticIsPlaying !== null && optimisticIsPlaying === actualIsPlaying) {
      setOptimisticIsPlaying(null);
    }
  }, [actualIsPlaying, optimisticIsPlaying]);

  const playback: PlaybackState = useMemo(
    () => ({
      isPlaying,
      isLoaded,
      isBuffering,
      currentTime: position,
      duration,
    }),
    [isPlaying, isLoaded, isBuffering, position, duration]
  );

  // --- Actions ---

  const play = useCallback(async () => {
    setOptimisticIsPlaying(true);
    await TrackPlayer.play();
  }, []);

  const pause = useCallback(async () => {
    setOptimisticIsPlaying(false);
    await TrackPlayer.pause();
  }, []);

  const togglePlayPause = useCallback(async () => {
    try {
      const state = await TrackPlayer.getPlaybackState();

      if (state.state === State.Playing || state.state === State.Buffering || state.state === State.Loading) {
        setOptimisticIsPlaying(false);
        await TrackPlayer.pause();
      } else {
        setOptimisticIsPlaying(true);
        await TrackPlayer.play();
      }
    } catch (error) {
      console.error('Error toggling play/pause:', error);
    }
  }, []);

  const next = useCallback(async () => {
    if (queue.tracks.length === 0) return;
    const isLast = queue.currentIndex >= queue.tracks.length - 1;

    if (isLast && queue.repeatMode === 'off') return;

    await TrackPlayer.skipToNext();
    const nextIndex = isLast ? 0 : queue.currentIndex + 1;
    setQueueState((prev) => ({ ...prev, currentIndex: nextIndex }));
  }, [queue.currentIndex, queue.tracks.length, queue.repeatMode]);

  const previous = useCallback(async () => {
    if (queue.tracks.length === 0) return;

    if (position > 3) {
      await TrackPlayer.seekTo(0);
      return;
    }

    const isFirst = queue.currentIndex <= 0;

    if (isFirst && queue.repeatMode === 'off') {
      await TrackPlayer.seekTo(0);
      return;
    }

    await TrackPlayer.skipToPrevious();
    const prevIndex = isFirst
      ? queue.tracks.length - 1
      : queue.currentIndex - 1;
    setQueueState((prev) => ({ ...prev, currentIndex: prevIndex }));
  }, [queue.currentIndex, queue.tracks.length, queue.repeatMode, position]);

  const seekTo = useCallback(async (seconds: number) => {
    await TrackPlayer.seekTo(seconds);
  }, []);

  const setQueue = useCallback(
    async (tracks: Track[], startIndex = 0) => {
      isUpdatingQueue.current = true;
      originalOrderRef.current = tracks;

      const orderedTracks = queue.shuffleEnabled
        ? shuffleArray(tracks)
        : tracks;

      await TrackPlayer.reset();
      await TrackPlayer.add(orderedTracks.map(toRNTPTrack));

      if (startIndex > 0) {
        await TrackPlayer.skip(startIndex);
      }

      await TrackPlayer.play();

      setQueueState((prev) => ({
        ...prev,
        tracks: orderedTracks,
        currentIndex: startIndex,
      }));

      // Delay resetting the flag to ensure state update is processed first
      queueMicrotask(() => {
        isUpdatingQueue.current = false;
      });
    },
    [queue.shuffleEnabled]
  );

  const addToQueue = useCallback(
    async (tracks: Track[]) => {
      if (tracks.length === 0) return;
      isUpdatingQueue.current = true;

      await TrackPlayer.add(tracks.map(toRNTPTrack));
      originalOrderRef.current = [...originalOrderRef.current, ...tracks];

      setQueueState((prev) => ({
        ...prev,
        tracks: [...prev.tracks, ...tracks],
      }));

      // Delay resetting the flag to ensure state update is processed first
      queueMicrotask(() => {
        isUpdatingQueue.current = false;
      });
    },
    []
  );

  const selectTrack = useCallback(
    async (index: number) => {
      if (index < 0 || index >= queue.tracks.length) return;

      isUpdatingQueue.current = true;

      await TrackPlayer.skip(index);
      await TrackPlayer.play();

      setQueueState((prev) => ({ ...prev, currentIndex: index }));

      // Delay resetting the flag to ensure state update is processed first
      queueMicrotask(() => {
        isUpdatingQueue.current = false;
      });
    },
    [queue.tracks.length]
  );

  const toggleShuffle = useCallback(async () => {
    isUpdatingQueue.current = true;

    setQueueState((prev) => {
      const enabling = !prev.shuffleEnabled;
      const current = prev.tracks[prev.currentIndex];

      let newTracks: Track[];
      let newIndex: number;

      if (enabling) {
        const others = prev.tracks.filter((_, i) => i !== prev.currentIndex);
        newTracks = [current, ...shuffleArray(others)];
        newIndex = 0;
      } else {
        newTracks = originalOrderRef.current;
        const idx = originalOrderRef.current.findIndex(
          (t) => t.id === current?.id
        );
        newIndex = idx >= 0 ? idx : 0;
      }

      // Sync RNTP queue asynchronously
      (async () => {
        await TrackPlayer.reset();
        await TrackPlayer.add(newTracks.map(toRNTPTrack));
        await TrackPlayer.skip(newIndex);
        await TrackPlayer.play();
        // Delay resetting the flag to ensure state update is processed first
        queueMicrotask(() => {
          isUpdatingQueue.current = false;
        });
      })();

      return {
        ...prev,
        shuffleEnabled: enabling,
        tracks: newTracks,
        currentIndex: newIndex,
      };
    });
  }, []);

  const cycleRepeatMode = useCallback(() => {
    setQueueState((prev) => {
      const modes: RepeatMode[] = ['off', 'all', 'one'];
      const idx = modes.indexOf(prev.repeatMode);
      const next = modes[(idx + 1) % modes.length];
      return { ...prev, repeatMode: next };
    });
  }, []);

  return {
    playback,
    queue,
    currentTrack,
    play,
    pause,
    togglePlayPause,
    next,
    previous,
    seekTo,
    setQueue,
    addToQueue,
    selectTrack,
    toggleShuffle,
    cycleRepeatMode,
  };
}
