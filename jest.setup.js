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

// Mock expo-audio
jest.mock('expo-audio', () => ({
  useAudioPlayer: () => ({
    play: jest.fn(),
    pause: jest.fn(),
    seekTo: jest.fn(),
    replace: jest.fn(),
    remove: jest.fn(),
    loop: false,
  }),
  useAudioPlayerStatus: () => ({
    playing: false,
    isLoaded: false,
    isBuffering: false,
    currentTime: 0,
    duration: 0,
    didJustFinish: false,
    playbackRate: 1,
    mute: false,
    loop: false,
    shouldCorrectPitch: true,
  }),
  setAudioModeAsync: jest.fn(),
}));

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
