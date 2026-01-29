import React, { createContext, useContext } from 'react';
import { useMusicPlayer, MusicPlayer } from '../hooks/useMusicPlayer';

const MusicPlayerContext = createContext<MusicPlayer | null>(null);

export function MusicPlayerProvider({ children }: { children: React.ReactNode }) {
  const player = useMusicPlayer();

  return (
    <MusicPlayerContext.Provider value={player}>
      {children}
    </MusicPlayerContext.Provider>
  );
}

export function useMusicPlayerContext(): MusicPlayer {
  const context = useContext(MusicPlayerContext);
  if (!context) {
    throw new Error('useMusicPlayerContext must be used within a MusicPlayerProvider');
  }
  return context;
}
