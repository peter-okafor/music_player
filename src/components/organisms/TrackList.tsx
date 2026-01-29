import React, { memo, useCallback } from 'react';
import { View, StyleSheet } from 'react-native';
import { FlashList } from '@shopify/flash-list';
import { Track } from '../../types';
import { TrackItem } from '../molecules/TrackItem';

interface TrackListProps {
  tracks: Track[];
  activeTrackId: string | null;
  isPlaying: boolean;
  onTrackPress: (index: number) => void;
  onEndReached?: () => void;
  bottomPadding?: number;
  testID?: string;
}

function TrackListComponent({
  tracks,
  activeTrackId,
  isPlaying,
  onTrackPress,
  onEndReached,
  bottomPadding = 0,
  testID,
}: TrackListProps) {
  const renderItem = useCallback(
    ({ item, index }: { item: Track; index: number }) => (
      <TrackItem
        testID={`track-item-${item.id}`}
        track={item}
        index={index}
        isActive={item.id === activeTrackId}
        isPlaying={item.id === activeTrackId && isPlaying}
        onPress={onTrackPress}
      />
    ),
    [activeTrackId, isPlaying, onTrackPress]
  );

  const keyExtractor = useCallback((item: Track) => item.id, []);

  return (
    <View testID={testID} style={styles.container}>
      <FlashList
        data={tracks}
        renderItem={renderItem}
        keyExtractor={keyExtractor}
        onEndReached={onEndReached}
        onEndReachedThreshold={0.5}
        ListFooterComponent={
          bottomPadding > 0 ? <View style={{ height: bottomPadding }} /> : null
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
});

export const TrackList = memo(TrackListComponent);
