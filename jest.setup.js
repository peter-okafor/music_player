// Mock react-native-reanimated
jest.mock('react-native-reanimated', () => {
  const View = require('react-native').View;
  return {
    __esModule: true,
    default: {
      View,
      createAnimatedComponent: (component) => component,
      addWhitelistedNativeProps: () => {},
      addWhitelistedUIProps: () => {},
    },
    FadeIn: { duration: () => ({ delay: () => ({}) }) },
    FadeOut: { duration: () => ({}) },
    SlideInDown: { duration: () => ({}) },
    SlideOutDown: { duration: () => ({}) },
    SlideInUp: { delay: () => ({ duration: () => ({}) }), duration: () => ({}) },
    useAnimatedStyle: (fn) => fn(),
    useSharedValue: (val) => ({ value: val }),
    withTiming: (val) => val,
    withSpring: (val) => val,
    Easing: { inOut: () => {}, bezier: () => {} },
  };
});

// Mock expo-router
jest.mock('expo-router', () => ({
  useRouter: () => ({
    push: jest.fn(),
    back: jest.fn(),
    replace: jest.fn(),
  }),
  useLocalSearchParams: () => ({}),
  Stack: {
    Screen: 'Stack.Screen',
  },
  Link: 'Link',
}));

// Mock expo-image
jest.mock('expo-image', () => {
  const { View } = require('react-native');
  return {
    Image: View,
  };
});

// Mock lucide-react-native
jest.mock('lucide-react-native', () => {
  const React = require('react');
  const { View } = require('react-native');
  const icon = (props) => React.createElement(View, props);
  return {
    Play: icon,
    Pause: icon,
    SkipBack: icon,
    SkipForward: icon,
    Shuffle: icon,
    Repeat: icon,
    Repeat1: icon,
    ChevronDown: icon,
    Music: icon,
  };
});

// Mock react-native-track-player
jest.mock('react-native-track-player', () => {
  const State = {
    None: 'none',
    Playing: 'playing',
    Paused: 'paused',
    Stopped: 'stopped',
    Buffering: 'buffering',
    Loading: 'loading',
    Error: 'error',
    Ready: 'ready',
    Ended: 'ended',
  };

  const RepeatMode = {
    Off: 0,
    Track: 1,
    Queue: 2,
  };

  const Capability = {
    Play: 'play',
    Pause: 'pause',
    Stop: 'stop',
    SeekTo: 'seekto',
    Skip: 'skip',
    SkipToNext: 'skip_to_next',
    SkipToPrevious: 'skip_to_previous',
  };

  const Event = {
    RemotePlay: 'remote-play',
    RemotePause: 'remote-pause',
    RemoteStop: 'remote-stop',
    RemoteNext: 'remote-next',
    RemotePrevious: 'remote-previous',
    RemoteSeek: 'remote-seek',
    PlaybackState: 'playback-state',
    PlaybackActiveTrackChanged: 'playback-active-track-changed',
  };

  const AppKilledPlaybackBehavior = {
    ContinuePlayback: 'continue-playback',
    PausePlayback: 'pause-playback',
    StopPlaybackAndRemoveNotification: 'stop-playback-and-remove-notification',
  };

  return {
    __esModule: true,
    default: {
      setupPlayer: jest.fn().mockResolvedValue(undefined),
      updateOptions: jest.fn().mockResolvedValue(undefined),
      add: jest.fn().mockResolvedValue(undefined),
      reset: jest.fn().mockResolvedValue(undefined),
      skip: jest.fn().mockResolvedValue(undefined),
      skipToNext: jest.fn().mockResolvedValue(undefined),
      skipToPrevious: jest.fn().mockResolvedValue(undefined),
      play: jest.fn().mockResolvedValue(undefined),
      pause: jest.fn().mockResolvedValue(undefined),
      stop: jest.fn().mockResolvedValue(undefined),
      seekTo: jest.fn().mockResolvedValue(undefined),
      setRepeatMode: jest.fn().mockResolvedValue(undefined),
      getActiveTrack: jest.fn().mockResolvedValue(undefined),
      getProgress: jest.fn().mockResolvedValue({ position: 0, duration: 0, buffered: 0 }),
      getQueue: jest.fn().mockResolvedValue([]),
      registerPlaybackService: jest.fn(),
      addEventListener: jest.fn(() => ({ remove: jest.fn() })),
    },
    State,
    RepeatMode,
    Capability,
    Event,
    AppKilledPlaybackBehavior,
    usePlaybackState: jest.fn(() => ({ state: State.None })),
    useProgress: jest.fn(() => ({ position: 0, duration: 0, buffered: 0 })),
    useActiveTrack: jest.fn(() => undefined),
  };
});

// Mock expo-media-library
jest.mock('expo-media-library', () => ({
  getPermissionsAsync: jest.fn().mockResolvedValue({ status: 'granted' }),
  requestPermissionsAsync: jest.fn().mockResolvedValue({ status: 'granted' }),
  getAssetsAsync: jest.fn().mockResolvedValue({
    assets: [],
    endCursor: '',
    hasNextPage: false,
    totalCount: 0,
  }),
  SortBy: { default: 'default' },
}));

// Mock safe-area-context
jest.mock('react-native-safe-area-context', () => ({
  useSafeAreaInsets: () => ({ top: 0, bottom: 0, left: 0, right: 0 }),
  SafeAreaProvider: ({ children }) => children,
  SafeAreaView: ({ children }) => children,
}));
