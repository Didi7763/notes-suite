export interface User {
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  role: 'USER' | 'ADMIN';
  createdAt: string;
  updatedAt: string;
}

export interface Note {
  id: number;
  title: string;
  content: string;
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  isPublic: boolean;
  createdAt: string;
  updatedAt: string;
  user: User;
  tags?: NoteTag[];
  isOffline?: boolean;
  syncStatus?: 'synced' | 'pending' | 'error';
}

export interface NoteTag {
  id: number;
  name: string;
  color?: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
}

export interface OfflineNote {
  id: string;
  title: string;
  content: string;
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  isPublic: boolean;
  createdAt: string;
  updatedAt: string;
  isOffline: boolean;
  syncStatus: 'pending' | 'error';
}

export type RootStackParamList = {
  Login: undefined;
  Register: undefined;
  Main: undefined;
  NoteEditor: { noteId?: number };
  NoteViewer: { noteId: number };
};

export type MainTabParamList = {
  Notes: undefined;
  Public: undefined;
  Profile: undefined;
};
