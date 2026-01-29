import React from 'react';
import { View, Text, StyleSheet, Dimensions } from 'react-native';
import { Image } from 'expo-image';
import { useRouter } from 'expo-router';
import { ChevronDown, Music } from 'lucide-react-native';
import Animated, { FadeIn, SlideInUp } from 'react-native-reanimated';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useMusicPlayerContext } from '../src/context';
import { PlayerControls, IconButton } from '../src/components';
import { colors, spacing } from '../src/theme';

const { width: SCREEN_WIDTH } = Dimensions.get('window');
const ARTWORK_SIZE = SCREEN_WIDTH - spacing.xl * 2;

export default function PlayerScreen() {
  const router = useRouter();
  const insets = useSafeAreaInsets();
  const {
    currentTrack,
    playback,
    queue,
    togglePlayPause,
    next,
    previous,
    seekTo,
    toggleShuffle,
    cycleRepeatMode,
  } = useMusicPlayerContext();

  if (!currentTrack) {
    return (
      <View style={[styles.container, { paddingTop: insets.top }]}>
        <Text style={styles.emptyText}>No track selected</Text>
      </View>
    );
  }

  return (
    <View style={[styles.container, { paddingTop: insets.top }]}>
      {/* Header */}
      <View style={styles.header}>
        <IconButton
          testID="close-player"
          onPress={() => router.back()}
          size={44}
          icon={<ChevronDown size={28} color={colors.text} />}
        />
        <View style={styles.headerCenter}>
          <Text style={styles.headerLabel}>PLAYING FROM</Text>
          <Text style={styles.headerAlbum} numberOfLines={1}>
            {currentTrack.album}
          </Text>
        </View>
        <View style={{ width: 44 }} />
      </View>

      {/* Artwork */}
      <Animated.View
        entering={FadeIn.duration(400)}
        style={styles.artworkContainer}
      >
        {currentTrack.artwork ? (
          <Image
            source={{ uri: currentTrack.artwork }}
            style={[styles.artwork, { width: ARTWORK_SIZE, height: ARTWORK_SIZE }]}
            contentFit="cover"
            transition={300}
          />
        ) : (
          <View style={[styles.artwork, styles.placeholderArt, { width: ARTWORK_SIZE, height: ARTWORK_SIZE }]}>
            <Music size={80} color={colors.textMuted} />
          </View>
        )}
      </Animated.View>

      {/* Track info */}
      <Animated.View
        entering={SlideInUp.delay(100).duration(300)}
        style={styles.trackInfo}
      >
        <Text style={styles.title} numberOfLines={1}>
          {currentTrack.title}
        </Text>
        <Text style={styles.artist} numberOfLines={1}>
          {currentTrack.artist}
        </Text>
      </Animated.View>

      {/* Controls */}
      <Animated.View entering={SlideInUp.delay(200).duration(300)}>
        <PlayerControls
          testID="player-controls"
          isPlaying={playback.isPlaying}
          currentTime={playback.currentTime}
          duration={playback.duration}
          shuffleEnabled={queue.shuffleEnabled}
          repeatMode={queue.repeatMode}
          onTogglePlayPause={togglePlayPause}
          onNext={next}
          onPrevious={previous}
          onSeek={seekTo}
          onToggleShuffle={toggleShuffle}
          onCycleRepeat={cycleRepeatMode}
        />
      </Animated.View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.sm,
  },
  headerCenter: {
    flex: 1,
    alignItems: 'center',
  },
  headerLabel: {
    color: colors.textMuted,
    fontSize: 11,
    fontWeight: '600',
    letterSpacing: 1,
  },
  headerAlbum: {
    color: colors.text,
    fontSize: 13,
    fontWeight: '600',
    marginTop: 2,
  },
  artworkContainer: {
    alignItems: 'center',
    paddingVertical: spacing.lg,
  },
  artwork: {
    borderRadius: 12,
    backgroundColor: colors.surface,
  },
  placeholderArt: {
    justifyContent: 'center',
    alignItems: 'center',
  },
  trackInfo: {
    paddingHorizontal: spacing.lg,
    paddingBottom: spacing.lg,
  },
  title: {
    color: colors.text,
    fontSize: 22,
    fontWeight: '700',
  },
  artist: {
    color: colors.textSecondary,
    fontSize: 16,
    marginTop: spacing.xs,
  },
  emptyText: {
    color: colors.textSecondary,
    fontSize: 16,
    textAlign: 'center',
    marginTop: 100,
  },
});
