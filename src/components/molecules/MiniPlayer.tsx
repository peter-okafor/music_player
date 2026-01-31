import React, { memo } from 'react';
import { View, Text, Pressable, StyleSheet } from 'react-native';
import { Image } from 'expo-image';
import { Play, Pause, SkipForward, Music } from 'lucide-react-native';
import Animated, {
  SlideInDown,
  SlideOutDown,
} from 'react-native-reanimated';
import { useRouter } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Track } from '../../types';
import { ProgressBar } from '../atoms/ProgressBar';
import { IconButton } from '../atoms/IconButton';
import { colors, spacing } from '../../theme';

interface MiniPlayerProps {
  track: Track;
  isPlaying: boolean;
  progress: number;
  onTogglePlayPause: () => void;
  onNext: () => void;
  testID?: string;
}

function MiniPlayerComponent({
  track,
  isPlaying,
  progress,
  onTogglePlayPause,
  onNext,
  testID,
}: MiniPlayerProps) {
  const router = useRouter();
  const insets = useSafeAreaInsets();

  return (
    <Animated.View
      testID={testID}
      entering={SlideInDown.duration(300)}
      exiting={SlideOutDown.duration(200)}
      style={[styles.wrapper, { paddingBottom: insets.bottom }]}
    >
      <ProgressBar progress={progress} height={2} />
      <Pressable
        style={styles.container}
        onPress={() => router.push('/player')}
      >
        {track.artwork ? (
          <Image
            source={{ uri: track.artwork }}
            style={styles.artwork}
            contentFit="cover"
            transition={200}
          />
        ) : (
          <View style={[styles.artwork, styles.placeholderArt]}>
            <Music size={18} color={colors.textMuted} />
          </View>
        )}
        <View style={styles.info}>
          <Text style={styles.title} numberOfLines={1}>
            {track.title}
          </Text>
          <Text style={styles.artist} numberOfLines={1}>
            {track.artist}
          </Text>
        </View>
        <View style={styles.controls}>
          <IconButton
            testID="mini-play-pause"
            onPress={onTogglePlayPause}
            size={40}
            icon={
              isPlaying ? (
                <Pause size={22} color={colors.text} fill={colors.text} />
              ) : (
                <Play size={22} color={colors.text} fill={colors.text} />
              )
            }
          />
          <IconButton
            testID="mini-next"
            onPress={onNext}
            size={40}
            icon={<SkipForward size={20} color={colors.text} fill={colors.text} />}
          />
        </View>
      </Pressable>
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    backgroundColor: colors.surface,
    borderTopWidth: StyleSheet.hairlineWidth,
    borderTopColor: colors.border,
  },
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.sm,
    gap: spacing.sm,
  },
  artwork: {
    width: 44,
    height: 44,
    borderRadius: 6,
    backgroundColor: colors.surfaceLight,
  },
  placeholderArt: {
    justifyContent: 'center',
    alignItems: 'center',
  },
  info: {
    flex: 1,
    gap: 2,
  },
  title: {
    color: colors.text,
    fontSize: 14,
    fontWeight: '600',
  },
  artist: {
    color: colors.textSecondary,
    fontSize: 12,
  },
  controls: {
    flexDirection: 'row',
    alignItems: 'center',
  },
});

export const MiniPlayer = memo(MiniPlayerComponent);
