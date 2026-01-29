import React from 'react';
import { render } from '@testing-library/react-native';
import { ProgressBar } from '../../src/components/atoms/ProgressBar';

describe('ProgressBar', () => {
  it('renders without crashing', () => {
    const { getByTestId } = render(
      <ProgressBar testID="progress" progress={0.5} />
    );

    expect(getByTestId('progress')).toBeTruthy();
  });

  it('clamps progress to 0-1 range', () => {
    // Should not throw for out-of-range values
    const { rerender } = render(
      <ProgressBar testID="progress" progress={-0.5} />
    );

    rerender(<ProgressBar testID="progress" progress={1.5} />);
    rerender(<ProgressBar testID="progress" progress={0} />);
    rerender(<ProgressBar testID="progress" progress={1} />);
  });

  it('renders with custom height and colors', () => {
    const { getByTestId } = render(
      <ProgressBar
        testID="progress"
        progress={0.3}
        height={10}
        trackColor="#000"
        progressColor="#FFF"
      />
    );

    expect(getByTestId('progress')).toBeTruthy();
  });
});
