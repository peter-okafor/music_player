import React, { useEffect, useState } from 'react';
import { View, StyleSheet, ActivityIndicator } from 'react-native';
import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { MusicPlayerProvider, useMusicPlayerContext } from '../src/context';
import { MiniPlayer } from '../src/components';
import { colors } from '../src/theme';
import { setupTrackPlayer } from '../src/services/trackPlayerSetup';

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
  const [isPlayerReady, setIsPlayerReady] = useState(false);

  useEffect(() => {
    setupTrackPlayer().then((ready) => {
      setIsPlayerReady(ready);
    });
  }, []);

  if (!isPlayerReady) {
    return (
      <GestureHandlerRootView style={styles.root}>
        <View style={styles.loading}>
          <ActivityIndicator size="large" color={colors.primary} />
        </View>
      </GestureHandlerRootView>
    );
  }

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
  loading: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: colors.background,
  },
});
