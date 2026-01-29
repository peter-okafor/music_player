import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {
  useAudioPlayer,
  useAudioPlayerStatus,
  setAudioModeAsync,
} from 'expo-audio';
import { Track, RepeatMode, PlaybackState, QueueState } from '../types';

export interface MusicPlayerActions {
  play: () => void;
  pause: () => void;
  togglePlayPause: () => void;
  next: () => void;
  previous: () => void;
  seekTo: (seconds: number) => void;
  setQueue: (tracks: Track[], startIndex?: number) => void;
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

export function useMusicPlayer(): MusicPlayer {
  const [queue, setQueueState] = useState<QueueState>({
    tracks: [],
    currentIndex: -1,
    shuffleEnabled: false,
    repeatMode: 'off',
  });

  const [originalOrder, setOriginalOrder] = useState<Track[]>([]);
  const hasInitialized = useRef(false);

  const currentTrack = useMemo(
    () =>
      queue.currentIndex >= 0 && queue.currentIndex < queue.tracks.length
        ? queue.tracks[queue.currentIndex]
        : null,
    [queue.currentIndex, queue.tracks]
  );

  const audioSource = useMemo(
    () => (currentTrack ? { uri: currentTrack.uri } : null),
    [currentTrack]
  );

  const player = useAudioPlayer(audioSource, { updateInterval: 250 });
  const status = useAudioPlayerStatus(player);

  // Configure audio mode on mount
  useEffect(() => {
    setAudioModeAsync({
      playsInSilentMode: true,
      shouldPlayInBackground: true,
      interruptionMode: 'doNotMix',
    });
  }, []);

  // Auto-play when a new track source loads
  useEffect(() => {
    if (status.isLoaded && currentTrack && !hasInitialized.current) {
      hasInitialized.current = true;
      player.play();
    }
  }, [status.isLoaded, currentTrack, player]);

  // Handle track end → advance to next
  useEffect(() => {
    if (!status.didJustFinish) return;

    if (queue.repeatMode === 'one') {
      player.seekTo(0);
      player.play();
      return;
    }

    const isLast = queue.currentIndex >= queue.tracks.length - 1;

    if (isLast && queue.repeatMode === 'off') {
      // Stop at end of queue
      return;
    }

    // Advance: wrap to 0 if repeat-all and at end
    const nextIndex = isLast ? 0 : queue.currentIndex + 1;
    hasInitialized.current = false;
    setQueueState((prev) => ({ ...prev, currentIndex: nextIndex }));
  }, [status.didJustFinish, queue.currentIndex, queue.tracks.length, queue.repeatMode, player]);

  const playback: PlaybackState = useMemo(
    () => ({
      isPlaying: status.playing,
      isLoaded: status.isLoaded,
      isBuffering: status.isBuffering,
      currentTime: status.currentTime,
      duration: status.duration,
    }),
    [status.playing, status.isLoaded, status.isBuffering, status.currentTime, status.duration]
  );

  // --- Actions ---

  const play = useCallback(() => {
    player.play();
  }, [player]);

  const pause = useCallback(() => {
    player.pause();
  }, [player]);

  const togglePlayPause = useCallback(() => {
    if (status.playing) {
      player.pause();
    } else {
      player.play();
    }
  }, [player, status.playing]);

  const next = useCallback(() => {
    if (queue.tracks.length === 0) return;
    const isLast = queue.currentIndex >= queue.tracks.length - 1;
    const nextIndex =
      isLast && queue.repeatMode !== 'off' ? 0 : isLast ? queue.currentIndex : queue.currentIndex + 1;
    if (nextIndex === queue.currentIndex && queue.repeatMode === 'one') {
      player.seekTo(0);
      player.play();
      return;
    }
    hasInitialized.current = false;
    setQueueState((prev) => ({ ...prev, currentIndex: nextIndex }));
  }, [queue.currentIndex, queue.tracks.length, queue.repeatMode, player]);

  const previous = useCallback(() => {
    if (queue.tracks.length === 0) return;

    // If we're more than 3 seconds in, restart current track
    if (status.currentTime > 3) {
      player.seekTo(0);
      return;
    }

    const isFirst = queue.currentIndex <= 0;
    const prevIndex =
      isFirst && queue.repeatMode !== 'off'
        ? queue.tracks.length - 1
        : isFirst
          ? 0
          : queue.currentIndex - 1;
    hasInitialized.current = false;
    setQueueState((prev) => ({ ...prev, currentIndex: prevIndex }));
  }, [queue.currentIndex, queue.tracks.length, queue.repeatMode, status.currentTime, player]);

  const seekTo = useCallback(
    (seconds: number) => {
      player.seekTo(seconds);
    },
    [player]
  );

  const setQueue = useCallback(
    (tracks: Track[], startIndex = 0) => {
      setOriginalOrder(tracks);
      hasInitialized.current = false;
      setQueueState((prev) => ({
        ...prev,
        tracks: prev.shuffleEnabled ? shuffleArray(tracks) : tracks,
        currentIndex: startIndex,
      }));
    },
    []
  );

  const selectTrack = useCallback(
    (index: number) => {
      if (index < 0 || index >= queue.tracks.length) return;
      hasInitialized.current = false;
      setQueueState((prev) => ({ ...prev, currentIndex: index }));
    },
    [queue.tracks.length]
  );

  const toggleShuffle = useCallback(() => {
    setQueueState((prev) => {
      const enabling = !prev.shuffleEnabled;
      const current = prev.tracks[prev.currentIndex];

      if (enabling) {
        const others = prev.tracks.filter((_, i) => i !== prev.currentIndex);
        const shuffled = [current, ...shuffleArray(others)];
        return { ...prev, shuffleEnabled: true, tracks: shuffled, currentIndex: 0 };
      }

      // Restoring original order
      const idx = originalOrder.findIndex((t) => t.id === current?.id);
      return {
        ...prev,
        shuffleEnabled: false,
        tracks: originalOrder,
        currentIndex: idx >= 0 ? idx : 0,
      };
    });
  }, [originalOrder]);

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
    selectTrack,
    toggleShuffle,
    cycleRepeatMode,
  };
}
