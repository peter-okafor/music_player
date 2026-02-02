import TrackPlayer, {
  Capability,
  AppKilledPlaybackBehavior,
} from 'react-native-track-player';

let isSetup = false;

export async function setupTrackPlayer(): Promise<boolean> {
  if (isSetup) return true;

  try {
    await TrackPlayer.setupPlayer({
      waitForBuffer: true,
      autoHandleInterruptions: true,
    });

    await TrackPlayer.updateOptions({
      capabilities: [
        Capability.Play,
        Capability.Pause,
        Capability.SkipToNext,
        Capability.SkipToPrevious,
        Capability.SeekTo,
        Capability.Stop,
      ],
      compactCapabilities: [
        Capability.Play,
        Capability.Pause,
        Capability.SkipToNext,
      ],
      notificationCapabilities: [
        Capability.Play,
        Capability.Pause,
        Capability.SkipToNext,
        Capability.SkipToPrevious,
      ],
      android: {
        appKilledPlaybackBehavior:
          AppKilledPlaybackBehavior.StopPlaybackAndRemoveNotification,
      },
    });

    isSetup = true;
    return true;
  } catch {
    // setupPlayer throws if already initialized (e.g., after hot reload)
    const currentTrack = await TrackPlayer.getActiveTrack();
    if (currentTrack !== undefined) {
      isSetup = true;
      return true;
    }
    return false;
  }
}
