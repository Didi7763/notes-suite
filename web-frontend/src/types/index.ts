// Types pour l'authentification
export interface User {
  id: number;
  email: string;
  createdAt: string;
  lastLoginAt?: string;
  isActive: boolean;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  userId: number;
  email: string;
  issuedAt: string;
  expiresAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
}

// Types pour les notes
export enum NoteVisibility {
  PRIVATE = 'PRIVATE',
  SHARED = 'SHARED',
  PUBLIC = 'PUBLIC'
}

export interface Tag {
  id: number;
  label: string;
  description?: string;
  color?: string;
  usageCount: number;
}

export interface Note {
  id: number;
  ownerId: number;
  ownerEmail: string;
  title: string;
  contentMd: string;
  visibility: NoteVisibility;
  createdAt: string;
  updatedAt: string;
  viewCount: number;
  isFavorite: boolean;
  tags: Tag[];
  shares?: Share[];
  publicLinks?: PublicLink[];
}

export interface NoteCreateRequest {
  title: string;
  contentMd: string;
  visibility: NoteVisibility;
  tags: string[];
}

export interface NoteUpdateRequest {
  title: string;
  contentMd: string;
  visibility: NoteVisibility;
  tags: string[];
}

// Types pour le partage
export enum SharePermission {
  READ = 'READ',
  WRITE = 'WRITE',
  ADMIN = 'ADMIN'
}

export interface Share {
  id: number;
  noteId: number;
  noteTitle: string;
  sharedWithUserId: number;
  sharedWithUserEmail: string;
  permission: SharePermission;
  createdAt: string;
  updatedAt: string;
  expiresAt?: string;
  isActive: boolean;
}

export interface ShareCreateRequest {
  userEmail: string;
  permission: SharePermission;
  expiresAt?: string;
}

// Types pour les liens publics
export interface PublicLink {
  id: number;
  noteId: number;
  noteTitle: string;
  urlToken: string;
  expiresAt?: string;
  createdAt: string;
  updatedAt: string;
  accessCount: number;
  maxAccessCount?: number;
  isActive: boolean;
  description?: string;
  isPasswordProtected: boolean;
}

export interface PublicLinkCreateRequest {
  expiresAt?: string;
  maxAccessCount?: number;
  description?: string;
  password?: string;
}

// Types pour les réponses API
export interface ApiResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  size: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
}

export interface ErrorResponse {
  success: false;
  message: string;
  details: string;
  timestamp: string;
}

// Types pour les filtres et recherche
export interface NoteFilters {
  query?: string;
  tag?: string;
  visibility?: NoteVisibility;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
}

// Types pour l'état de l'application
export interface AuthState {
  user: User | null;
  token: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

export interface AppState {
  auth: AuthState;
  notes: {
    items: Note[];
    totalCount: number;
    isLoading: boolean;
    error: string | null;
  };
}


