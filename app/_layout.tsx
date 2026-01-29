import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { MusicPlayerProvider, useMusicPlayerContext } from '../src/context';
import { MiniPlayer } from '../src/components';
import { colors } from '../src/theme';

function MiniPlayerOverlay() {
  const { currentTrack, playback, togglePlayPause, next } =
    useMusicPlayerContext();

  if (!currentTrack) return null;

  const progress =
    playback.duration > 0 ? playback.currentTime / playback.duration : 0;

  return (
    <MiniPlayer
      testID="mini-player"
      track={currentTrack}
      isPlaying={playback.isPlaying}
      progress={progress}
      onTogglePlayPause={togglePlayPause}
      onNext={next}
    />
  );
}

function RootLayoutInner() {
  return (
    <View style={styles.root}>
      <Stack
        screenOptions={{
          headerStyle: { backgroundColor: colors.background },
          headerTintColor: colors.text,
          headerTitleStyle: { fontWeight: '600' },
          contentStyle: { backgroundColor: colors.background },
          animation: 'slide_from_bottom',
        }}
      >
        <Stack.Screen
          name="index"
          options={{
            title: 'Music Player',
          }}
        />
        <Stack.Screen
          name="player"
          options={{
            title: '',
            headerTransparent: true,
            presentation: 'modal',
            animation: 'slide_from_bottom',
          }}
        />
      </Stack>
      <MiniPlayerOverlay />
      <StatusBar style="light" />
    </View>
  );
}

export default function RootLayout() {
  return (
    <GestureHandlerRootView style={styles.root}>
      <MusicPlayerProvider>
        <RootLayoutInner />
      </MusicPlayerProvider>
    </GestureHandlerRootView>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: colors.background,
  },
});
