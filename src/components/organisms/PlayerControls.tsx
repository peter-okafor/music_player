import React, { memo, useCallback, useMemo } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import {
  Play,
  Pause,
  SkipBack,
  SkipForward,
  Shuffle,
  Repeat,
  Repeat1,
} from 'lucide-react-native';
import { RepeatMode } from '../../types';
import { IconButton } from '../atoms/IconButton';
import { ProgressBar } from '../atoms/ProgressBar';
import { colors, spacing } from '../../theme';

interface PlayerControlsProps {
  isPlaying: boolean;
  currentTime: number;
  duration: number;
  shuffleEnabled: boolean;
  repeatMode: RepeatMode;
  onTogglePlayPause: () => void;
  onNext: () => void;
  onPrevious: () => void;
  onSeek: (seconds: number) => void;
  onToggleShuffle: () => void;
  onCycleRepeat: () => void;
  testID?: string;
}

function formatTime(seconds: number): string {
  const mins = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${mins}:${secs.toString().padStart(2, '0')}`;
}

function PlayerControlsComponent({
  isPlaying,
  currentTime,
  duration,
  shuffleEnabled,
  repeatMode,
  onTogglePlayPause,
  onNext,
  onPrevious,
  onSeek,
  onToggleShuffle,
  onCycleRepeat,
  testID,
}: PlayerControlsProps) {
  const progress = duration > 0 ? currentTime / duration : 0;

  const handleSeek = useCallback(
    (ratio: number) => {
      onSeek(ratio * duration);
    },
    [onSeek, duration]
  );

  const repeatIcon = useMemo(() => {
    const isActive = repeatMode !== 'off';
    const iconColor = isActive ? colors.primary : colors.textSecondary;

    if (repeatMode === 'one') {
      return <Repeat1 size={22} color={iconColor} />;
    }
    return <Repeat size={22} color={iconColor} />;
  }, [repeatMode]);

  return (
    <View testID={testID} style={styles.container}>
      {/* Progress */}
      <View style={styles.progressSection}>
        <ProgressBar
          testID="player-progress"
          progress={progress}
          onSeek={handleSeek}
          height={4}
        />
        <View style={styles.timeRow}>
          <Text style={styles.time}>{formatTime(currentTime)}</Text>
          <Text style={styles.time}>{formatTime(duration)}</Text>
        </View>
      </View>

      {/* Controls */}
      <View style={styles.controlsRow}>
        <IconButton
          testID="shuffle-btn"
          onPress={onToggleShuffle}
          size={44}
          icon={
            <Shuffle
              size={22}
              color={shuffleEnabled ? colors.primary : colors.textSecondary}
            />
          }
        />
        <IconButton
          testID="prev-btn"
          onPress={onPrevious}
          size={52}
          icon={<SkipBack size={28} color={colors.text} fill={colors.text} />}
        />
        <IconButton
          testID="play-pause-btn"
          onPress={onTogglePlayPause}
          size={64}
          style={styles.playButton}
          icon={
            isPlaying ? (
              <Pause size={30} color={colors.background} fill={colors.background} />
            ) : (
              <Play size={30} color={colors.background} fill={colors.background} />
            )
          }
        />
        <IconButton
          testID="next-btn"
          onPress={onNext}
          size={52}
          icon={<SkipForward size={28} color={colors.text} fill={colors.text} />}
        />
        <IconButton
          testID="repeat-btn"
          onPress={onCycleRepeat}
          size={44}
          icon={repeatIcon}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: spacing.md,
    paddingHorizontal: spacing.lg,
  },
  progressSection: {
    gap: spacing.xs,
  },
  timeRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  time: {
    color: colors.textSecondary,
    fontSize: 12,
    fontVariant: ['tabular-nums'],
  },
  controlsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: spacing.md,
  },
  playButton: {
    backgroundColor: colors.text,
    borderRadius: 32,
  },
});

export const PlayerControls = memo(PlayerControlsComponent);
