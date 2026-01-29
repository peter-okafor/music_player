import React, { memo, useCallback } from 'react';
import {
  View,
  StyleSheet,
  GestureResponderEvent,
  LayoutChangeEvent,
} from 'react-native';
import { colors } from '../../theme';

interface ProgressBarProps {
  progress: number; // 0–1
  onSeek?: (progress: number) => void;
  height?: number;
  trackColor?: string;
  progressColor?: string;
  testID?: string;
}

function ProgressBarComponent({
  progress,
  onSeek,
  height = 4,
  trackColor = colors.surfaceLight,
  progressColor = colors.primary,
  testID,
}: ProgressBarProps) {
  const widthRef = React.useRef(0);

  const onLayout = useCallback((e: LayoutChangeEvent) => {
    widthRef.current = e.nativeEvent.layout.width;
  }, []);

  const handleTouch = useCallback(
    (e: GestureResponderEvent) => {
      if (!onSeek || widthRef.current === 0) return;
      const x = e.nativeEvent.locationX;
      const ratio = Math.max(0, Math.min(1, x / widthRef.current));
      onSeek(ratio);
    },
    [onSeek]
  );

  const clampedProgress = Math.max(0, Math.min(1, progress));

  return (
    <View
      testID={testID}
      style={[styles.track, { height, backgroundColor: trackColor }]}
      onLayout={onLayout}
      onStartShouldSetResponder={() => !!onSeek}
      onResponderRelease={handleTouch}
      onResponderMove={handleTouch}
    >
      <View
        style={[
          styles.fill,
          {
            backgroundColor: progressColor,
            width: `${clampedProgress * 100}%`,
          },
        ]}
      />
      {onSeek && (
        <View
          style={[
            styles.thumb,
            {
              left: `${clampedProgress * 100}%`,
            },
          ]}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  track: {
    width: '100%',
    borderRadius: 2,
    overflow: 'visible',
    position: 'relative',
    justifyContent: 'center',
  },
  fill: {
    height: '100%',
    borderRadius: 2,
  },
  thumb: {
    position: 'absolute',
    width: 14,
    height: 14,
    borderRadius: 7,
    backgroundColor: colors.text,
    marginLeft: -7,
    top: -5,
  },
});

export const ProgressBar = memo(ProgressBarComponent);
