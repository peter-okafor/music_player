import { renderHook, act, waitFor } from '@testing-library/react-native';
import * as MediaLibrary from 'expo-media-library';
import { useDeviceAudio } from '../../src/hooks/useDeviceAudio';

const mockGetPermissions = MediaLibrary.getPermissionsAsync as jest.Mock;
const mockRequestPermissions = MediaLibrary.requestPermissionsAsync as jest.Mock;
const mockGetAssets = MediaLibrary.getAssetsAsync as jest.Mock;

const FAKE_ASSETS: Partial<MediaLibrary.Asset>[] = [
  {
    id: 'a1',
    filename: 'song_one.mp3',
    uri: 'file:///storage/song_one.mp3',
    mediaType: 'audio',
    duration: 120,
    width: 0,
    height: 0,
    creationTime: Date.now(),
    modificationTime: Date.now(),
  },
  {
    id: 'a2',
    filename: 'song_two.flac',
    uri: 'file:///storage/song_two.flac',
    mediaType: 'audio',
    duration: 240,
    width: 0,
    height: 0,
    creationTime: Date.now(),
    modificationTime: Date.now(),
  },
];

describe('useDeviceAudio', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockGetPermissions.mockResolvedValue({ status: 'undetermined' });
    mockRequestPermissions.mockResolvedValue({ status: 'granted' });
    mockGetAssets.mockResolvedValue({
      assets: FAKE_ASSETS,
      endCursor: 'cursor1',
      hasNextPage: false,
      totalCount: 2,
    });
  });

  it('starts with undetermined permission and no tracks', async () => {
    const { result } = renderHook(() => useDeviceAudio());

    // Initially undetermined (before the async check resolves)
    expect(result.current.tracks).toHaveLength(0);
    expect(result.current.isLoading).toBe(false);
  });

  it('loads tracks after permission is granted', async () => {
    mockGetPermissions.mockResolvedValue({ status: 'granted' });

    const { result } = renderHook(() => useDeviceAudio());

    await waitFor(() => {
      expect(result.current.tracks).toHaveLength(2);
    });

    expect(result.current.permissionStatus).toBe('granted');
    expect(result.current.totalCount).toBe(2);
    expect(result.current.tracks[0].title).toBe('song_one');
    expect(result.current.tracks[0].artwork).toBeNull();
    expect(result.current.tracks[1].title).toBe('song_two');
  });

  it('requests permission and loads tracks', async () => {
    mockGetPermissions.mockResolvedValue({ status: 'undetermined' });

    const { result } = renderHook(() => useDeviceAudio());

    // Wait for initial permission check
    await waitFor(() => {
      expect(result.current.permissionStatus).toBe('undetermined');
    });

    // Request permission
    await act(async () => {
      await result.current.requestPermission();
    });

    expect(mockRequestPermissions).toHaveBeenCalledWith(false, ['audio']);
    expect(result.current.permissionStatus).toBe('granted');

    // Tracks should load after permission granted
    await waitFor(() => {
      expect(result.current.tracks).toHaveLength(2);
    });
  });

  it('handles denied permission', async () => {
    mockGetPermissions.mockResolvedValue({ status: 'undetermined' });
    mockRequestPermissions.mockResolvedValue({ status: 'denied' });

    const { result } = renderHook(() => useDeviceAudio());

    await waitFor(() => {
      expect(result.current.permissionStatus).toBe('undetermined');
    });

    await act(async () => {
      await result.current.requestPermission();
    });

    expect(result.current.permissionStatus).toBe('denied');
    expect(result.current.tracks).toHaveLength(0);
  });

  it('strips file extension from filename for title', async () => {
    mockGetPermissions.mockResolvedValue({ status: 'granted' });

    const { result } = renderHook(() => useDeviceAudio());

    await waitFor(() => {
      expect(result.current.tracks).toHaveLength(2);
    });

    expect(result.current.tracks[0].title).toBe('song_one');
    expect(result.current.tracks[1].title).toBe('song_two');
  });

  it('supports pagination via loadMore', async () => {
    mockGetPermissions.mockResolvedValue({ status: 'granted' });
    mockGetAssets
      .mockResolvedValueOnce({
        assets: [FAKE_ASSETS[0]],
        endCursor: 'cursor1',
        hasNextPage: true,
        totalCount: 2,
      })
      .mockResolvedValueOnce({
        assets: [FAKE_ASSETS[1]],
        endCursor: 'cursor2',
        hasNextPage: false,
        totalCount: 2,
      });

    const { result } = renderHook(() => useDeviceAudio());

    await waitFor(() => {
      expect(result.current.tracks).toHaveLength(1);
    });

    expect(result.current.hasMore).toBe(true);

    await act(async () => {
      await result.current.loadMore();
    });

    expect(result.current.tracks).toHaveLength(2);
    expect(result.current.hasMore).toBe(false);
  });
});
