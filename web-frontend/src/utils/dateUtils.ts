import { format, formatDistanceToNow, parseISO, isValid } from 'date-fns';
import { fr } from 'date-fns/locale';

/**
 * Formate une date ISO en format français
 */
export const formatDate = (dateString: string, formatStr: string = 'dd/MM/yyyy'): string => {
  try {
    const date = parseISO(dateString);
    if (!isValid(date)) {
      return 'Date invalide';
    }
    return format(date, formatStr, { locale: fr });
  } catch (error) {
    return 'Date invalide';
  }
};

/**
 * Formate une date en format relatif (il y a X temps)
 */
export const formatRelativeTime = (dateString: string): string => {
  try {
    const date = parseISO(dateString);
    if (!isValid(date)) {
      return 'Date invalide';
    }
    return formatDistanceToNow(date, { addSuffix: true, locale: fr });
  } catch (error) {
    return 'Date invalide';
  }
};

/**
 * Formate une date complète avec heure
 */
export const formatDateTime = (dateString: string): string => {
  return formatDate(dateString, 'dd/MM/yyyy à HH:mm');
};

/**
 * Formate une date courte
 */
export const formatShortDate = (dateString: string): string => {
  return formatDate(dateString, 'dd/MM/yy');
};

/**
 * Vérifie si une date est récente (moins de 24h)
 */
export const isRecent = (dateString: string): boolean => {
  try {
    const date = parseISO(dateString);
    if (!isValid(date)) {
      return false;
    }
    const now = new Date();
    const diffInHours = (now.getTime() - date.getTime()) / (1000 * 60 * 60);
    return diffInHours < 24;
  } catch (error) {
    return false;
  }
};


