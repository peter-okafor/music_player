import TrackPlayer, { Event, State } from 'react-native-track-player';

export async function PlaybackService() {
  console.log('🎵 PlaybackService initialized');

  let wasPlaying = false;

  // Track when playback state changes
  TrackPlayer.addEventListener(Event.PlaybackState, (event) => {
    console.log('🎵 PlaybackState in service:', event.state);
    wasPlaying = event.state === State.Playing || event.state === State.Buffering || event.state === State.Loading;
  });

  // When track changes, resume playback if we were playing
  TrackPlayer.addEventListener(Event.PlaybackActiveTrackChanged, async (event) => {
    console.log('🎵 PlaybackActiveTrackChanged in service:', event);

    // If we were playing and track changed, auto-play the new track
    if (wasPlaying && event.track) {
      console.log('🎵 Auto-playing next track after change');
      try {
        await TrackPlayer.play();
      } catch (e) {
        console.warn('PlaybackService: Error auto-playing next track', e);
      }
    }
  });

  TrackPlayer.addEventListener(Event.RemotePlay, () => {
    try {
      TrackPlayer.play();
    } catch (e) {
      console.warn('PlaybackService: Error on RemotePlay', e);
    }
  });

  TrackPlayer.addEventListener(Event.RemotePause, () => {
    try {
      TrackPlayer.pause();
    } catch (e) {
      console.warn('PlaybackService: Error on RemotePause', e);
    }
  });

  TrackPlayer.addEventListener(Event.RemoteStop, () => {
    try {
      TrackPlayer.stop();
    } catch (e) {
      console.warn('PlaybackService: Error on RemoteStop', e);
    }
  });

  TrackPlayer.addEventListener(Event.RemoteNext, () => {
    try {
      TrackPlayer.skipToNext();
    } catch (e) {
      console.warn('PlaybackService: Error on RemoteNext', e);
    }
  });

  TrackPlayer.addEventListener(Event.RemotePrevious, async () => {
    try {
      const { position } = await TrackPlayer.getProgress();
      if (position > 3) {
        await TrackPlayer.seekTo(0);
      } else {
        await TrackPlayer.skipToPrevious();
      }
    } catch (e) {
      console.warn('PlaybackService: Error on RemotePrevious', e);
    }
  });

  TrackPlayer.addEventListener(Event.RemoteSeek, (event) => {
    try {
      TrackPlayer.seekTo(event.position);
    } catch (e) {
      console.warn('PlaybackService: Error on RemoteSeek', e);
    }
  });
}
