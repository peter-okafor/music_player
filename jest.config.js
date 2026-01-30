module.exports = {
  preset: 'jest-expo',
  setupFiles: ['./jest.setup.js'],
  transformIgnorePatterns: [
    'node_modules/(?!((jest-)?react-native|@react-native(-community)?)|expo(nent)?|@expo(nent)?/.*|@expo-google-fonts/.*|react-navigation|@react-navigation/.*|@sentry/react-native|native-base|react-native-svg|lucide-react-native|@shopify/flash-list|expo-image|react-native-track-player|expo-router|expo-linking|expo-constants|expo-status-bar|react-native-reanimated|react-native-gesture-handler|react-native-screens|react-native-safe-area-context)',
  ],
  testPathIgnorePatterns: ['/node_modules/', '__tests__/fixtures\\.ts$'],
  moduleFileExtensions: ['ts', 'tsx', 'js', 'jsx'],
  collectCoverageFrom: [
    'src/**/*.{ts,tsx}',
    '!src/**/index.ts',
    '!src/types/**',
  ],
};
