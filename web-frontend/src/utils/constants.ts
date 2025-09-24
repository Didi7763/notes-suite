/**
 * Constantes de l'application
 */

// Configuration de l'API
export const API_CONFIG = {
  BASE_URL: '/api/v1',
  TIMEOUT: 10000,
} as const;

// Configuration de la pagination
export const PAGINATION = {
  DEFAULT_PAGE_SIZE: 20,
  PAGE_SIZE_OPTIONS: [10, 20, 50, 100],
  MAX_PAGE_SIZE: 100,
} as const;

// Configuration des notes
export const NOTE_CONFIG = {
  MAX_TITLE_LENGTH: 255,
  MAX_CONTENT_LENGTH: 10000,
  MAX_TAGS_PER_NOTE: 10,
  MAX_TAG_LABEL_LENGTH: 50,
} as const;

// Configuration des utilisateurs
export const USER_CONFIG = {
  MIN_PASSWORD_LENGTH: 6,
  MAX_PASSWORD_LENGTH: 40,
  MAX_EMAIL_LENGTH: 255,
} as const;

// Configuration des liens publics
export const PUBLIC_LINK_CONFIG = {
  MAX_ACCESS_COUNT: 10000,
  DEFAULT_EXPIRY_DAYS: 30,
  MAX_EXPIRY_DAYS: 365,
  TOKEN_LENGTH: 32,
} as const;

// Messages d'erreur
export const ERROR_MESSAGES = {
  NETWORK_ERROR: 'Erreur de connexion. Vérifiez votre connexion internet.',
  UNAUTHORIZED: 'Vous n\'êtes pas autorisé à effectuer cette action.',
  FORBIDDEN: 'Accès refusé.',
  NOT_FOUND: 'Ressource non trouvée.',
  SERVER_ERROR: 'Erreur interne du serveur. Veuillez réessayer plus tard.',
  VALIDATION_ERROR: 'Les données fournies ne sont pas valides.',
  TIMEOUT_ERROR: 'La requête a expiré. Veuillez réessayer.',
} as const;

// Messages de succès
export const SUCCESS_MESSAGES = {
  NOTE_CREATED: 'Note créée avec succès',
  NOTE_UPDATED: 'Note mise à jour avec succès',
  NOTE_DELETED: 'Note supprimée avec succès',
  NOTE_SHARED: 'Note partagée avec succès',
  SHARE_REVOKED: 'Partage révoqué avec succès',
  PUBLIC_LINK_CREATED: 'Lien public créé avec succès',
  PUBLIC_LINK_DELETED: 'Lien public supprimé avec succès',
  USER_REGISTERED: 'Compte créé avec succès',
  USER_LOGGED_IN: 'Connexion réussie',
  USER_LOGGED_OUT: 'Déconnexion réussie',
} as const;

// Routes de l'application
export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  REGISTER: '/register',
  DASHBOARD: '/dashboard',
  NOTES: '/notes',
  NOTE_EDIT: '/notes/:id/edit',
  NOTE_VIEW: '/notes/:id',
  NOTE_CREATE: '/notes/create',
  FAVORITES: '/favorites',
  SHARED: '/shared',
  PUBLIC: '/public',
  PUBLIC_NOTE: '/p/:token',
  PROFILE: '/profile',
  SETTINGS: '/settings',
} as const;

// Configuration du thème
export const THEME_CONFIG = {
  PRIMARY_COLOR: '#1976d2',
  SECONDARY_COLOR: '#dc004e',
  SUCCESS_COLOR: '#2e7d32',
  WARNING_COLOR: '#ed6c02',
  ERROR_COLOR: '#d32f2f',
  INFO_COLOR: '#0288d1',
} as const;

// Configuration du localStorage
export const STORAGE_KEYS = {
  ACCESS_TOKEN: 'accessToken',
  REFRESH_TOKEN: 'refreshToken',
  USER_PREFERENCES: 'userPreferences',
  THEME: 'theme',
  LANGUAGE: 'language',
} as const;

// Configuration des notifications
export const NOTIFICATION_CONFIG = {
  DEFAULT_DURATION: 4000,
  SUCCESS_DURATION: 3000,
  ERROR_DURATION: 6000,
  WARNING_DURATION: 5000,
  INFO_DURATION: 4000,
} as const;


