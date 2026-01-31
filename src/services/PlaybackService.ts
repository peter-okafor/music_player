import TrackPlayer, { Event, State } from 'react-native-track-player';

export async function PlaybackService() {
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
