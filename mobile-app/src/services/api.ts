import axios from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';
import NetInfo from '@react-native-community/netinfo';
import { 
  AuthResponse, 
  LoginRequest, 
  RegisterRequest, 
  Note, 
  User,
  OfflineNote 
} from '../types';

const API_BASE_URL = 'http://10.0.2.2:8080/api/v1'; // Pour Android emulator

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Intercepteur pour ajouter le token JWT
api.interceptors.request.use(
  async (config) => {
    const token = await AsyncStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Intercepteur pour gérer les erreurs de réponse
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      await AsyncStorage.multiRemove(['token', 'user']);
    }
    return Promise.reject(error);
  }
);

export const authService = {
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post('/auth/signin', credentials);
    return response.data;
  },

  register: async (userData: RegisterRequest): Promise<void> => {
    await api.post('/auth/signup', userData);
  },

  logout: async () => {
    await AsyncStorage.multiRemove(['token', 'user']);
  },
};

export const noteService = {
  getNotes: async (): Promise<Note[]> => {
    const response = await api.get('/notes');
    return response.data.content || response.data;
  },

  getNote: async (id: number): Promise<Note> => {
    const response = await api.get(`/notes/${id}`);
    return response.data;
  },

  createNote: async (note: Partial<Note>): Promise<Note> => {
    const response = await api.post('/notes', note);
    return response.data;
  },

  updateNote: async (id: number, note: Partial<Note>): Promise<Note> => {
    const response = await api.put(`/notes/${id}`, note);
    return response.data;
  },

  deleteNote: async (id: number): Promise<void> => {
    await api.delete(`/notes/${id}`);
  },

  getPublicNotes: async (): Promise<Note[]> => {
    const response = await api.get('/notes/public');
    return response.data;
  },

  searchNotes: async (query: string): Promise<Note[]> => {
    const response = await api.get(`/notes/search?q=${encodeURIComponent(query)}`);
    return response.data;
  },
};

export const userService = {
  getProfile: async (): Promise<User> => {
    const response = await api.get('/users/profile');
    return response.data;
  },

  updateProfile: async (userData: Partial<User>): Promise<User> => {
    const response = await api.put('/users/profile', userData);
    return response.data;
  },
};

// Service pour vérifier la connectivité
export const networkService = {
  isConnected: async (): Promise<boolean> => {
    const netInfo = await NetInfo.fetch();
    return netInfo.isConnected ?? false;
  },

  onConnectivityChange: (callback: (isConnected: boolean) => void) => {
    return NetInfo.addEventListener(callback);
  },
};

export default api;
