import React from 'react';
import { render, fireEvent } from '@testing-library/react-native';
import { Text } from 'react-native';
import { IconButton } from '../../src/components/atoms/IconButton';

describe('IconButton', () => {
  it('renders without crashing', () => {
    const { getByTestId } = render(
      <IconButton
        testID="icon-btn"
        onPress={jest.fn()}
        icon={<Text>X</Text>}
      />
    );

    expect(getByTestId('icon-btn')).toBeTruthy();
  });

  it('calls onPress when pressed', () => {
    const onPress = jest.fn();
    const { getByTestId } = render(
      <IconButton
        testID="icon-btn"
        onPress={onPress}
        icon={<Text>X</Text>}
      />
    );

    fireEvent.press(getByTestId('icon-btn'));
    expect(onPress).toHaveBeenCalledTimes(1);
  });

  it('does not call onPress when disabled', () => {
    const onPress = jest.fn();
    const { getByTestId } = render(
      <IconButton
        testID="icon-btn"
        onPress={onPress}
        icon={<Text>X</Text>}
        disabled
      />
    );

    fireEvent.press(getByTestId('icon-btn'));
    expect(onPress).not.toHaveBeenCalled();
  });
});
