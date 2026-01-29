import { renderHook, act } from '@testing-library/react-native';
import { useMusicPlayer } from '../../src/hooks/useMusicPlayer';
import { TEST_TRACKS } from '../fixtures';

// Mock expo-audio with controllable state
const mockPlay = jest.fn();
const mockPause = jest.fn();
const mockSeekTo = jest.fn();
let mockStatus = {
  playing: false,
  isLoaded: false,
  isBuffering: false,
  currentTime: 0,
  duration: 0,
  didJustFinish: false,
  playbackRate: 1,
  mute: false,
  loop: false,
  shouldCorrectPitch: true,
};

jest.mock('expo-audio', () => ({
  useAudioPlayer: () => ({
    play: mockPlay,
    pause: mockPause,
    seekTo: mockSeekTo,
    replace: jest.fn(),
    remove: jest.fn(),
    loop: false,
  }),
  useAudioPlayerStatus: () => mockStatus,
  setAudioModeAsync: jest.fn(),
}));

describe('useMusicPlayer', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockStatus = {
      playing: false,
      isLoaded: false,
      isBuffering: false,
      currentTime: 0,
      duration: 0,
      didJustFinish: false,
      playbackRate: 1,
      mute: false,
      loop: false,
      shouldCorrectPitch: true,
    };
  });

  it('initializes with empty queue and no current track', () => {
    const { result } = renderHook(() => useMusicPlayer());

    expect(result.current.currentTrack).toBeNull();
    expect(result.current.queue.tracks).toHaveLength(0);
    expect(result.current.queue.currentIndex).toBe(-1);
    expect(result.current.queue.shuffleEnabled).toBe(false);
    expect(result.current.queue.repeatMode).toBe('off');
  });

  it('sets queue and selects first track', () => {
    const { result } = renderHook(() => useMusicPlayer());

    act(() => {
      result.current.setQueue(TEST_TRACKS, 0);
    });

    expect(result.current.queue.tracks).toHaveLength(TEST_TRACKS.length);
    expect(result.current.queue.currentIndex).toBe(0);
    expect(result.current.currentTrack).toEqual(TEST_TRACKS[0]);
  });

  it('selects a track by index', () => {
    const { result } = renderHook(() => useMusicPlayer());

    act(() => {
      result.current.setQueue(TEST_TRACKS, 0);
    });

    act(() => {
      result.current.selectTrack(2);
    });

    expect(result.current.queue.currentIndex).toBe(2);
    expect(result.current.currentTrack).toEqual(TEST_TRACKS[2]);
  });

  it('ignores selectTrack with invalid index', () => {
    const { result } = renderHook(() => useMusicPlayer());

    act(() => {
      result.current.setQueue(TEST_TRACKS, 0);
    });

    act(() => {
      result.current.selectTrack(999);
    });

    expect(result.current.queue.currentIndex).toBe(0);
  });

  it('calls player.play on play()', () => {
    const { result } = renderHook(() => useMusicPlayer());

    act(() => {
      result.current.play();
    });

    expect(mockPlay).toHaveBeenCalled();
  });

  it('calls player.pause on pause()', () => {
    const { result } = renderHook(() => useMusicPlayer());

    act(() => {
      result.current.pause();
    });

    expect(mockPause).toHaveBeenCalled();
  });

  it('togglePlayPause calls pause when playing', () => {
    mockStatus = { ...mockStatus, playing: true };
    const { result } = renderHook(() => useMusicPlayer());

    act(() => {
      result.current.togglePlayPause();
    });

    expect(mockPause).toHaveBeenCalled();
  });

  it('togglePlayPause calls play when paused', () => {
    mockStatus = { ...mockStatus, playing: false };
    const { result } = renderHook(() => useMusicPlayer());

    act(() => {
      result.current.togglePlayPause();
    });

    expect(mockPlay).toHaveBeenCalled();
  });

  it('advances to next track', () => {
    const { result } = renderHook(() => useMusicPlayer());

    act(() => {
      result.current.setQueue(TEST_TRACKS, 0);
    });

    act(() => {
      result.current.next();
    });

    expect(result.current.queue.currentIndex).toBe(1);
  });

  it('goes to previous track when at start of track', () => {
    mockStatus = { ...mockStatus, currentTime: 0 };
    const { result } = renderHook(() => useMusicPlayer());

    act(() => {
      result.current.setQueue(TEST_TRACKS, 2);
    });

    act(() => {
      result.current.previous();
    });

    expect(result.current.queue.currentIndex).toBe(1);
  });

  it('restarts current track when more than 3 seconds in', () => {
    mockStatus = { ...mockStatus, currentTime: 5 };
    const { result } = renderHook(() => useMusicPlayer());

    act(() => {
      result.current.setQueue(TEST_TRACKS, 2);
    });

    act(() => {
      result.current.previous();
    });

    expect(mockSeekTo).toHaveBeenCalledWith(0);
    expect(result.current.queue.currentIndex).toBe(2);
  });

  it('calls seekTo with correct value', () => {
    const { result } = renderHook(() => useMusicPlayer());

    act(() => {
      result.current.seekTo(30);
    });

    expect(mockSeekTo).toHaveBeenCalledWith(30);
  });

  it('cycles repeat mode through off → all → one → off', () => {
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

  it('toggles shuffle on and off', () => {
    const { result } = renderHook(() => useMusicPlayer());

    act(() => {
      result.current.setQueue(TEST_TRACKS, 0);
    });

    const originalFirst = result.current.queue.tracks[0];

    act(() => {
      result.current.toggleShuffle();
    });

    expect(result.current.queue.shuffleEnabled).toBe(true);
    // Current track should remain at index 0 after shuffle
    expect(result.current.queue.currentIndex).toBe(0);
    expect(result.current.queue.tracks[0].id).toBe(originalFirst.id);

    act(() => {
      result.current.toggleShuffle();
    });

    expect(result.current.queue.shuffleEnabled).toBe(false);
    // Should restore original order
    expect(result.current.queue.tracks).toEqual(TEST_TRACKS);
  });

  it('reports playback state from status', () => {
    mockStatus = {
      ...mockStatus,
      playing: true,
      isLoaded: true,
      isBuffering: false,
      currentTime: 45,
      duration: 200,
    };

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
