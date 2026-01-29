import React from 'react';
import { render, fireEvent } from '@testing-library/react-native';
import { MiniPlayer } from '../../src/components/molecules/MiniPlayer';
import { TEST_TRACKS } from '../fixtures';

// Animated.View needs to be a plain View for tests
jest.mock('react-native-reanimated', () => {
  const { View } = require('react-native');
  return {
    __esModule: true,
    default: {
      View,
      createAnimatedComponent: (comp: unknown) => comp,
    },
    FadeIn: { duration: () => ({ delay: () => ({}) }) },
    FadeOut: { duration: () => ({}) },
    SlideInDown: { duration: () => ({}) },
    SlideOutDown: { duration: () => ({}) },
    SlideInUp: { delay: () => ({ duration: () => ({}) }), duration: () => ({}) },
  };
});

describe('MiniPlayer', () => {
  const defaultProps = {
    track: TEST_TRACKS[0],
    isPlaying: false,
    progress: 0.5,
    onTogglePlayPause: jest.fn(),
    onNext: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders track info', () => {
    const { getByText } = render(<MiniPlayer {...defaultProps} />);

    expect(getByText('Test Song One')).toBeTruthy();
    expect(getByText('Artist A')).toBeTruthy();
  });

  it('calls onTogglePlayPause when play/pause pressed', () => {
    const onTogglePlayPause = jest.fn();
    const { getByTestId } = render(
      <MiniPlayer {...defaultProps} onTogglePlayPause={onTogglePlayPause} />
    );

    fireEvent.press(getByTestId('mini-play-pause'));
    expect(onTogglePlayPause).toHaveBeenCalledTimes(1);
  });

  it('calls onNext when next pressed', () => {
    const onNext = jest.fn();
    const { getByTestId } = render(
      <MiniPlayer {...defaultProps} onNext={onNext} />
    );

    fireEvent.press(getByTestId('mini-next'));
    expect(onNext).toHaveBeenCalledTimes(1);
  });

  it('renders placeholder when artwork is null', () => {
    const trackNoArt = { ...TEST_TRACKS[0], artwork: null };
    const { getByText } = render(
      <MiniPlayer {...defaultProps} track={trackNoArt} />
    );

    expect(getByText('Test Song One')).toBeTruthy();
  });
});
