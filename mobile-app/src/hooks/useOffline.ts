import { useState, useEffect, createContext, useContext, ReactNode } from 'react';
import NetInfo from '@react-native-community/netinfo';
import { networkService } from '../services/api';

interface OfflineContextType {
  isConnected: boolean;
  isOfflineMode: boolean;
}

const OfflineContext = createContext<OfflineContextType | undefined>(undefined);

export const useOffline = () => {
  const context = useContext(OfflineContext);
  if (context === undefined) {
    throw new Error('useOffline must be used within an OfflineProvider');
  }
  return context;
};

interface OfflineProviderProps {
  children: ReactNode;
}

export const OfflineProvider = ({ children }: OfflineProviderProps) => {
  const [isConnected, setIsConnected] = useState(true);
  const [isOfflineMode, setIsOfflineMode] = useState(false);

  useEffect(() => {
    const unsubscribe = NetInfo.addEventListener(state => {
      const connected = state.isConnected ?? false;
      setIsConnected(connected);
      setIsOfflineMode(!connected);
    });

    return () => unsubscribe();
  }, []);

  const value = {
    isConnected,
    isOfflineMode,
  };

  return (
    <OfflineContext.Provider value={value}>
      {children}
    </OfflineContext.Provider>
  );
};
