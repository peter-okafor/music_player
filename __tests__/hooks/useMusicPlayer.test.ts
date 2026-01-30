import { renderHook, act } from '@testing-library/react-native';
import TrackPlayer, {
  State,
  usePlaybackState,
  useProgress,
  useActiveTrack,
} from 'react-native-track-player';
import { useMusicPlayer } from '../../src/hooks/useMusicPlayer';
import { TEST_TRACKS } from '../fixtures';

const mockTrackPlayer = TrackPlayer as jest.Mocked<typeof TrackPlayer>;
const mockUsePlaybackState = usePlaybackState as jest.MockedFunction<
  typeof usePlaybackState
>;
const mockUseProgress = useProgress as jest.MockedFunction<typeof useProgress>;
const mockUseActiveTrack = useActiveTrack as jest.MockedFunction<
  typeof useActiveTrack
>;

describe('useMusicPlayer', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockUsePlaybackState.mockReturnValue({ state: State.None } as ReturnType<
      typeof usePlaybackState
    >);
    mockUseProgress.mockReturnValue({
      position: 0,
      duration: 0,
      buffered: 0,
    });
    mockUseActiveTrack.mockReturnValue(undefined);
  });

  it('initializes with empty queue and no current track', () => {
    const { result } = renderHook(() => useMusicPlayer());

    expect(result.current.currentTrack).toBeNull();
    expect(result.current.queue.tracks).toHaveLength(0);
    expect(result.current.queue.currentIndex).toBe(-1);
    expect(result.current.queue.shuffleEnabled).toBe(false);
    expect(result.current.queue.repeatMode).toBe('off');
  });

  it('sets queue and selects first track', async () => {
    const { result } = renderHook(() => useMusicPlayer());

    await act(async () => {
      await result.current.setQueue(TEST_TRACKS, 0);
    });

    expect(result.current.queue.tracks).toHaveLength(TEST_TRACKS.length);
    expect(result.current.queue.currentIndex).toBe(0);
    expect(result.current.currentTrack).toEqual(TEST_TRACKS[0]);
    expect(mockTrackPlayer.reset).toHaveBeenCalled();
    expect(mockTrackPlayer.add).toHaveBeenCalled();
    expect(mockTrackPlayer.play).toHaveBeenCalled();
  });

  it('appends tracks to the queue without resetting playback', async () => {
    const { result } = renderHook(() => useMusicPlayer());

    await act(async () => {
      await result.current.setQueue(TEST_TRACKS.slice(0, 2), 0);
    });

    mockTrackPlayer.reset.mockClear();
    mockTrackPlayer.play.mockClear();

    await act(async () => {
      await result.current.addToQueue(TEST_TRACKS.slice(2));
    });

    expect(result.current.queue.tracks).toHaveLength(TEST_TRACKS.length);
    expect(result.current.queue.currentIndex).toBe(0);
    expect(mockTrackPlayer.reset).not.toHaveBeenCalled();
    expect(mockTrackPlayer.play).not.toHaveBeenCalled();
    expect(mockTrackPlayer.add).toHaveBeenCalledWith(
      TEST_TRACKS.slice(2).map((t) => ({
        id: t.id,
        url: t.uri,
        title: t.title,
        artist: t.artist,
        album: t.album,
        artwork: undefined,
        duration: t.duration,
      }))
    );
  });

  it('selects a track by index', async () => {
    const { result } = renderHook(() => useMusicPlayer());

    await act(async () => {
      await result.current.setQueue(TEST_TRACKS, 0);
    });

    await act(async () => {
      await result.current.selectTrack(2);
    });

    expect(result.current.queue.currentIndex).toBe(2);
    expect(result.current.currentTrack).toEqual(TEST_TRACKS[2]);
    expect(mockTrackPlayer.skip).toHaveBeenCalledWith(2);
  });

  it('ignores selectTrack with invalid index', async () => {
    const { result } = renderHook(() => useMusicPlayer());

    await act(async () => {
      await result.current.setQueue(TEST_TRACKS, 0);
    });

    await act(async () => {
      await result.current.selectTrack(999);
    });

    expect(result.current.queue.currentIndex).toBe(0);
  });

  it('calls TrackPlayer.play on play()', () => {
    const { result } = renderHook(() => useMusicPlayer());

    act(() => {
      result.current.play();
    });

    expect(mockTrackPlayer.play).toHaveBeenCalled();
  });

  it('calls TrackPlayer.pause on pause()', () => {
    const { result } = renderHook(() => useMusicPlayer());

    act(() => {
      result.current.pause();
    });

    expect(mockTrackPlayer.pause).toHaveBeenCalled();
  });

  it('togglePlayPause calls pause when playing', () => {
    mockUsePlaybackState.mockReturnValue({
      state: State.Playing,
    } as ReturnType<typeof usePlaybackState>);
    const { result } = renderHook(() => useMusicPlayer());

    act(() => {
      result.current.togglePlayPause();
    });

    expect(mockTrackPlayer.pause).toHaveBeenCalled();
  });

  it('togglePlayPause calls play when paused', () => {
    mockUsePlaybackState.mockReturnValue({ state: State.Paused } as ReturnType<
      typeof usePlaybackState
    >);
    const { result } = renderHook(() => useMusicPlayer());

    act(() => {
      result.current.togglePlayPause();
    });

    expect(mockTrackPlayer.play).toHaveBeenCalled();
  });

  it('advances to next track', async () => {
    const { result } = renderHook(() => useMusicPlayer());

    await act(async () => {
      await result.current.setQueue(TEST_TRACKS, 0);
    });

    await act(async () => {
      await result.current.next();
    });

    expect(result.current.queue.currentIndex).toBe(1);
    expect(mockTrackPlayer.skipToNext).toHaveBeenCalled();
  });

  it('goes to previous track when at start of track', async () => {
    mockUseProgress.mockReturnValue({
      position: 0,
      duration: 200,
      buffered: 0,
    });
    const { result } = renderHook(() => useMusicPlayer());

    await act(async () => {
      await result.current.setQueue(TEST_TRACKS, 2);
    });

    await act(async () => {
      await result.current.previous();
    });

    expect(result.current.queue.currentIndex).toBe(1);
    expect(mockTrackPlayer.skipToPrevious).toHaveBeenCalled();
  });

  it('restarts current track when more than 3 seconds in', async () => {
    mockUseProgress.mockReturnValue({
      position: 5,
      duration: 200,
      buffered: 0,
    });
    const { result } = renderHook(() => useMusicPlayer());

    await act(async () => {
      await result.current.setQueue(TEST_TRACKS, 2);
    });

    await act(async () => {
      await result.current.previous();
    });

    expect(mockTrackPlayer.seekTo).toHaveBeenCalledWith(0);
    expect(result.current.queue.currentIndex).toBe(2);
  });

  it('calls seekTo with correct value', async () => {
    const { result } = renderHook(() => useMusicPlayer());

    await act(async () => {
      await result.current.seekTo(30);
    });

    expect(mockTrackPlayer.seekTo).toHaveBeenCalledWith(30);
  });

  it('cycles repeat mode through off -> all -> one -> off', () => {
    const { result } = renderHook(() => useMusicPlayer());

    expect(result.current.queue.repeatMode).toBe('off');

    act(() => {
      result.current.cycleRepeatMode();
    });
    expect(result.current.queue.repeatMode).toBe('all');

    act(() => {
      result.current.cycleRepeatMode();
    });
    expect(result.current.queue.repeatMode).toBe('one');

    act(() => {
      result.current.cycleRepeatMode();
    });
    expect(result.current.queue.repeatMode).toBe('off');
  });

  it('toggles shuffle on and off', async () => {
    const { result } = renderHook(() => useMusicPlayer());

    await act(async () => {
      await result.current.setQueue(TEST_TRACKS, 0);
    });

    const originalFirst = result.current.queue.tracks[0];

    await act(async () => {
      await result.current.toggleShuffle();
    });

    expect(result.current.queue.shuffleEnabled).toBe(true);
    // Current track should remain at index 0 after shuffle
    expect(result.current.queue.currentIndex).toBe(0);
    expect(result.current.queue.tracks[0].id).toBe(originalFirst.id);

    await act(async () => {
      await result.current.toggleShuffle();
    });

    expect(result.current.queue.shuffleEnabled).toBe(false);
    // Should restore original order
    expect(result.current.queue.tracks).toEqual(TEST_TRACKS);
  });

  it('reports playback state from RNTP hooks', () => {
    mockUsePlaybackState.mockReturnValue({
      state: State.Playing,
    } as ReturnType<typeof usePlaybackState>);
    mockUseProgress.mockReturnValue({
      position: 45,
      duration: 200,
      buffered: 100,
    });

    const { result } = renderHook(() => useMusicPlayer());

    expect(result.current.playback).toEqual({
      isPlaying: true,
      isLoaded: true,
      isBuffering: false,
      currentTime: 45,
      duration: 200,
    });
  });
});
