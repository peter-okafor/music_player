import React, { memo, useCallback } from 'react';
import { View, Text, Pressable, StyleSheet } from 'react-native';
import { Image } from 'expo-image';
import { Play, Pause, Music } from 'lucide-react-native';
import { Track } from '../../types';
import { colors, spacing } from '../../theme';

interface TrackItemProps {
  track: Track;
  index: number;
  isActive: boolean;
  isPlaying: boolean;
  onPress: (index: number) => void;
  testID?: string;
}

function TrackItemComponent({
  track,
  index,
  isActive,
  isPlaying,
  onPress,
  testID,
}: TrackItemProps) {
  const handlePress = useCallback(() => {
    onPress(index);
  }, [onPress, index]);

  return (
    <Pressable
      testID={testID}
      onPress={handlePress}
      style={({ pressed }) => [
        styles.container,
        isActive && styles.active,
        pressed && styles.pressed,
      ]}
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
          <Music size={22} color={colors.textMuted} />
        </View>
      )}
      <View style={styles.info}>
        <Text
          style={[styles.title, isActive && styles.activeText]}
          numberOfLines={1}
        >
          {track.title}
        </Text>
        <Text style={styles.artist} numberOfLines={1}>
          {track.artist}
        </Text>
      </View>
      {isActive && (
        <View style={styles.indicator}>
          {isPlaying ? (
            <Pause size={18} color={colors.primary} fill={colors.primary} />
          ) : (
            <Play size={18} color={colors.primary} fill={colors.primary} />
          )}
        </View>
      )}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    gap: spacing.md,
  },
  active: {
    backgroundColor: colors.surfaceLight,
  },
  pressed: {
    opacity: 0.7,
  },
  artwork: {
    width: 52,
    height: 52,
    borderRadius: 6,
    backgroundColor: colors.surface,
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
    fontSize: 16,
    fontWeight: '500',
  },
  activeText: {
    color: colors.primary,
  },
  artist: {
    color: colors.textSecondary,
    fontSize: 13,
  },
  indicator: {
    paddingRight: spacing.xs,
  },
});

export const TrackItem = memo(TrackItemComponent);
