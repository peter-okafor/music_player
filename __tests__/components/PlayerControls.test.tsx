import React from 'react';
import { render, fireEvent } from '@testing-library/react-native';
import { PlayerControls } from '../../src/components/organisms/PlayerControls';

describe('PlayerControls', () => {
  const defaultProps = {
    isPlaying: false,
    currentTime: 30,
    duration: 200,
    shuffleEnabled: false,
    repeatMode: 'off' as const,
    onTogglePlayPause: jest.fn(),
    onNext: jest.fn(),
    onPrevious: jest.fn(),
    onSeek: jest.fn(),
    onToggleShuffle: jest.fn(),
    onCycleRepeat: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders without crashing', () => {
    const { getByTestId } = render(
      <PlayerControls {...defaultProps} testID="controls" />
    );

    expect(getByTestId('controls')).toBeTruthy();
  });

  it('displays formatted time', () => {
    const { getByText } = render(<PlayerControls {...defaultProps} />);

    expect(getByText('0:30')).toBeTruthy();
    expect(getByText('3:20')).toBeTruthy();
  });

  it('calls onTogglePlayPause when play/pause pressed', () => {
    const onTogglePlayPause = jest.fn();
    const { getByTestId } = render(
      <PlayerControls {...defaultProps} onTogglePlayPause={onTogglePlayPause} />
    );

    fireEvent.press(getByTestId('play-pause-btn'));
    expect(onTogglePlayPause).toHaveBeenCalledTimes(1);
  });

  it('calls onNext when next pressed', () => {
    const onNext = jest.fn();
    const { getByTestId } = render(
      <PlayerControls {...defaultProps} onNext={onNext} />
    );

    fireEvent.press(getByTestId('next-btn'));
    expect(onNext).toHaveBeenCalledTimes(1);
  });

  it('calls onPrevious when previous pressed', () => {
    const onPrevious = jest.fn();
    const { getByTestId } = render(
      <PlayerControls {...defaultProps} onPrevious={onPrevious} />
    );

    fireEvent.press(getByTestId('prev-btn'));
    expect(onPrevious).toHaveBeenCalledTimes(1);
  });

  it('calls onToggleShuffle when shuffle pressed', () => {
    const onToggleShuffle = jest.fn();
    const { getByTestId } = render(
      <PlayerControls {...defaultProps} onToggleShuffle={onToggleShuffle} />
    );

    fireEvent.press(getByTestId('shuffle-btn'));
    expect(onToggleShuffle).toHaveBeenCalledTimes(1);
  });

  it('calls onCycleRepeat when repeat pressed', () => {
    const onCycleRepeat = jest.fn();
    const { getByTestId } = render(
      <PlayerControls {...defaultProps} onCycleRepeat={onCycleRepeat} />
    );

    fireEvent.press(getByTestId('repeat-btn'));
    expect(onCycleRepeat).toHaveBeenCalledTimes(1);
  });
});
