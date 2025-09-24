/**
 * Utilitaires de validation pour les formulaires
 */

export const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/**
 * Valide une adresse email
 */
export const isValidEmail = (email: string): boolean => {
  return emailRegex.test(email);
};

/**
 * Valide un mot de passe (minimum 6 caractères)
 */
export const isValidPassword = (password: string): boolean => {
  return password.length >= 6;
};

/**
 * Valide un titre de note (non vide, max 255 caractères)
 */
export const isValidNoteTitle = (title: string): boolean => {
  return title.trim().length > 0 && title.length <= 255;
};

/**
 * Valide un label de tag (non vide, max 50 caractères)
 */
export const isValidTagLabel = (label: string): boolean => {
  return label.trim().length > 0 && label.length <= 50;
};

/**
 * Nettoie et valide un label de tag
 */
export const cleanTagLabel = (label: string): string => {
  return label.trim().toLowerCase().replace(/[^a-z0-9\s-]/g, '');
};

/**
 * Génère un message d'erreur pour un champ requis
 */
export const getRequiredMessage = (fieldName: string): string => {
  return `${fieldName} est obligatoire`;
};

/**
 * Génère un message d'erreur pour un champ trop court
 */
export const getMinLengthMessage = (fieldName: string, minLength: number): string => {
  return `${fieldName} doit contenir au moins ${minLength} caractères`;
};

/**
 * Génère un message d'erreur pour un champ trop long
 */
export const getMaxLengthMessage = (fieldName: string, maxLength: number): string => {
  return `${fieldName} ne peut pas dépasser ${maxLength} caractères`;
};

/**
 * Génère un message d'erreur pour un email invalide
 */
export const getInvalidEmailMessage = (): string => {
  return 'Format d\'email invalide';
};

/**
 * Génère un message d'erreur pour un mot de passe invalide
 */
export const getInvalidPasswordMessage = (): string => {
  return 'Le mot de passe doit contenir au moins 6 caractères';
};


