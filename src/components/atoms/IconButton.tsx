import React, { memo } from 'react';
import { Pressable, StyleSheet, ViewStyle } from 'react-native';
import { colors } from '../../theme';

interface IconButtonProps {
  onPress: () => void;
  icon: React.ReactNode;
  size?: number;
  style?: ViewStyle;
  disabled?: boolean;
  testID?: string;
}

function IconButtonComponent({
  onPress,
  icon,
  size = 48,
  style,
  disabled = false,
  testID,
}: IconButtonProps) {
  return (
    <Pressable
      testID={testID}
      onPress={onPress}
      disabled={disabled}
      style={({ pressed }) => [
        styles.container,
        { width: size, height: size, borderRadius: size / 2 },
        pressed && styles.pressed,
        disabled && styles.disabled,
        style,
      ]}
    >
      {icon}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  pressed: {
    opacity: 0.6,
  },
  disabled: {
    opacity: 0.3,
  },
});

export const IconButton = memo(IconButtonComponent);
