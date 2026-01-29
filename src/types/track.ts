export interface Track {
  id: string;
  title: string;
  artist: string;
  album: string;
  artwork: string | null;
  uri: string;
  duration: number; // seconds
}

export type RepeatMode = 'off' | 'all' | 'one';

export interface PlaybackState {
  isPlaying: boolean;
  isLoaded: boolean;
  isBuffering: boolean;
  currentTime: number; // seconds
  duration: number; // seconds
}

export interface QueueState {
  tracks: Track[];
  currentIndex: number;
  shuffleEnabled: boolean;
  repeatMode: RepeatMode;
}
