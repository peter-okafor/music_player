import React from 'react';
import { render, fireEvent } from '@testing-library/react-native';
import { TrackItem } from '../../src/components/molecules/TrackItem';
import { TEST_TRACKS } from '../fixtures';

describe('TrackItem', () => {
  const defaultProps = {
    track: TEST_TRACKS[0],
    index: 0,
    isActive: false,
    isPlaying: false,
    onPress: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders track title and artist', () => {
    const { getByText } = render(<TrackItem {...defaultProps} />);

    expect(getByText('Test Song One')).toBeTruthy();
    expect(getByText('Artist A')).toBeTruthy();
  });

  it('calls onPress with index when pressed', () => {
    const onPress = jest.fn();
    const { getByText } = render(
      <TrackItem {...defaultProps} index={2} onPress={onPress} />
    );

    fireEvent.press(getByText('Test Song One'));
    expect(onPress).toHaveBeenCalledWith(2);
  });

  it('renders without crashing when active', () => {
    const { getByText } = render(
      <TrackItem {...defaultProps} isActive isPlaying />
    );

    expect(getByText('Test Song One')).toBeTruthy();
  });

  it('renders placeholder when artwork is null', () => {
    const trackNoArt = { ...TEST_TRACKS[0], artwork: null };
    const { getByText } = render(
      <TrackItem {...defaultProps} track={trackNoArt} />
    );

    expect(getByText('Test Song One')).toBeTruthy();
  });
});
