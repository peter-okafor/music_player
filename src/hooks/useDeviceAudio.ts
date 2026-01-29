import { useCallback, useEffect, useState } from 'react';
import * as MediaLibrary from 'expo-media-library';
import { Track } from '../types';

export type PermissionStatus = 'undetermined' | 'granted' | 'denied';

export interface DeviceAudioState {
  tracks: Track[];
  isLoading: boolean;
  hasMore: boolean;
  permissionStatus: PermissionStatus;
  totalCount: number;
}

export interface DeviceAudioActions {
  requestPermission: () => Promise<void>;
  loadMore: () => Promise<void>;
  refresh: () => Promise<void>;
}

export type DeviceAudio = DeviceAudioState & DeviceAudioActions;

const PAGE_SIZE = 50;

function stripExtension(filename: string): string {
  return filename.replace(/\.[^.]+$/, '');
}

function assetToTrack(asset: MediaLibrary.Asset): Track {
  return {
    id: asset.id,
    title: stripExtension(asset.filename),
    artist: 'Unknown Artist',
    album: 'Unknown Album',
    artwork: null,
    uri: asset.uri,
    duration: asset.duration,
  };
}

export function useDeviceAudio(): DeviceAudio {
  const [tracks, setTracks] = useState<Track[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [hasMore, setHasMore] = useState(false);
  const [endCursor, setEndCursor] = useState<string | undefined>(undefined);
  const [permissionStatus, setPermissionStatus] =
    useState<PermissionStatus>('undetermined');
  const [totalCount, setTotalCount] = useState(0);

  // Check permission on mount
  useEffect(() => {
    (async () => {
      const { status } = await MediaLibrary.getPermissionsAsync(false, ['audio']);
      setPermissionStatus(status === 'granted' ? 'granted' : status === 'denied' ? 'denied' : 'undetermined');
    })();
  }, []);

  const requestPermission = useCallback(async () => {
    const { status } = await MediaLibrary.requestPermissionsAsync(
      false,
      ['audio']
    );
    const mapped: PermissionStatus =
      status === 'granted' ? 'granted' : 'denied';
    setPermissionStatus(mapped);
  }, []);

  const fetchPage = useCallback(
    async (after?: string) => {
      setIsLoading(true);
      try {
        const result = await MediaLibrary.getAssetsAsync({
          mediaType: 'audio',
          first: PAGE_SIZE,
          after,
          sortBy: [MediaLibrary.SortBy.default],
        });

        const newTracks = result.assets.map(assetToTrack);

        setTracks((prev) => (after ? [...prev, ...newTracks] : newTracks));
        setEndCursor(result.endCursor);
        setHasMore(result.hasNextPage);
        setTotalCount(result.totalCount);
      } catch {
        // Permission may have been revoked or not truly granted for audio
        setPermissionStatus('denied');
      } finally {
        setIsLoading(false);
      }
    },
    []
  );

  // Auto-load first page when permission is granted
  useEffect(() => {
    if (permissionStatus === 'granted') {
      fetchPage();
    }
  }, [permissionStatus, fetchPage]);

  const loadMore = useCallback(async () => {
    if (isLoading || !hasMore) return;
    await fetchPage(endCursor);
  }, [isLoading, hasMore, endCursor, fetchPage]);

  const refresh = useCallback(async () => {
    setEndCursor(undefined);
    await fetchPage();
  }, [fetchPage]);

  return {
    tracks,
    isLoading,
    hasMore,
    permissionStatus,
    totalCount,
    requestPermission,
    loadMore,
    refresh,
  };
}
