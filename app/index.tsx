import React, { useCallback, useEffect } from 'react';
import { View, Text, StyleSheet, ActivityIndicator, Pressable } from 'react-native';
import { useMusicPlayerContext } from '../src/context';
import { TrackList } from '../src/components';
import { useDeviceAudio } from '../src/hooks';
import { colors, spacing } from '../src/theme';

export default function HomeScreen() {
  const { setQueue, selectTrack, currentTrack, playback, queue } =
    useMusicPlayerContext();
  const {
    tracks: deviceTracks,
    isLoading,
    permissionStatus,
    totalCount,
    requestPermission,
    loadMore,
    hasMore,
  } = useDeviceAudio();

  useEffect(() => {
    if (deviceTracks.length > 0) {
      setQueue(deviceTracks);
    }
  }, [deviceTracks, setQueue]);

  const handleTrackPress = useCallback(
    (index: number) => {
      selectTrack(index);
    },
    [selectTrack]
  );

  const handleEndReached = useCallback(() => {
    if (hasMore && !isLoading) {
      loadMore();
    }
  }, [hasMore, isLoading, loadMore]);

  if (permissionStatus === 'undetermined') {
    return (
      <View style={styles.centered}>
        <Text style={styles.message}>
          Grant access to play music from your device.
        </Text>
        <Pressable style={styles.button} onPress={requestPermission}>
          <Text style={styles.buttonText}>Allow Access</Text>
        </Pressable>
      </View>
    );
  }

  if (permissionStatus === 'denied') {
    return (
      <View style={styles.centered}>
        <Text style={styles.message}>
          Media access was denied. Please enable it in your device settings to
          browse your music library.
        </Text>
        <Pressable style={styles.button} onPress={requestPermission}>
          <Text style={styles.buttonText}>Retry</Text>
        </Pressable>
      </View>
    );
  }

  if (isLoading && deviceTracks.length === 0) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color={colors.primary} />
        <Text style={styles.loadingText}>Scanning for audio files...</Text>
      </View>
    );
  }

  if (deviceTracks.length === 0) {
    return (
      <View style={styles.centered}>
        <Text style={styles.message}>No audio files found on this device.</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.heading}>Your Library</Text>
        <Text style={styles.subtitle}>
          {totalCount} {totalCount === 1 ? 'song' : 'songs'}
        </Text>
      </View>
      <TrackList
        testID="track-list"
        tracks={queue.tracks.length > 0 ? queue.tracks : deviceTracks}
        activeTrackId={currentTrack?.id ?? null}
        isPlaying={playback.isPlaying}
        onTrackPress={handleTrackPress}
        onEndReached={handleEndReached}
        bottomPadding={currentTrack ? 80 : 0}
      />
      {isLoading && deviceTracks.length > 0 && (
        <View style={styles.footerLoader}>
          <ActivityIndicator size="small" color={colors.primary} />
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  centered: {
    flex: 1,
    backgroundColor: colors.background,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: spacing.xl,
  },
  header: {
    paddingHorizontal: spacing.md,
    paddingTop: spacing.sm,
    paddingBottom: spacing.md,
  },
  heading: {
    color: colors.text,
    fontSize: 28,
    fontWeight: '700',
  },
  subtitle: {
    color: colors.textSecondary,
    fontSize: 14,
    marginTop: spacing.xs,
  },
  message: {
    color: colors.textSecondary,
    fontSize: 16,
    textAlign: 'center',
    lineHeight: 24,
  },
  button: {
    marginTop: spacing.lg,
    backgroundColor: colors.primary,
    paddingHorizontal: spacing.xl,
    paddingVertical: spacing.md,
    borderRadius: 24,
  },
  buttonText: {
    color: colors.text,
    fontSize: 16,
    fontWeight: '600',
  },
  loadingText: {
    color: colors.textSecondary,
    fontSize: 14,
    marginTop: spacing.md,
  },
  footerLoader: {
    paddingVertical: spacing.sm,
    alignItems: 'center',
  },
});
