import AsyncStorage from '@react-native-async-storage/async-storage';
import { OfflineNote } from '../types';

const OFFLINE_NOTES_KEY = 'offline_notes';
const PENDING_SYNC_KEY = 'pending_sync';

export const offlineStorage = {
  // Sauvegarder une note hors ligne
  saveOfflineNote: async (note: OfflineNote): Promise<void> => {
    try {
      const existingNotes = await offlineStorage.getOfflineNotes();
      const updatedNotes = [...existingNotes, note];
      await AsyncStorage.setItem(OFFLINE_NOTES_KEY, JSON.stringify(updatedNotes));
    } catch (error) {
      console.error('Error saving offline note:', error);
    }
  },

  // Récupérer toutes les notes hors ligne
  getOfflineNotes: async (): Promise<OfflineNote[]> => {
    try {
      const notes = await AsyncStorage.getItem(OFFLINE_NOTES_KEY);
      return notes ? JSON.parse(notes) : [];
    } catch (error) {
      console.error('Error getting offline notes:', error);
      return [];
    }
  },

  // Mettre à jour une note hors ligne
  updateOfflineNote: async (noteId: string, updates: Partial<OfflineNote>): Promise<void> => {
    try {
      const notes = await offlineStorage.getOfflineNotes();
      const updatedNotes = notes.map(note => 
        note.id === noteId ? { ...note, ...updates } : note
      );
      await AsyncStorage.setItem(OFFLINE_NOTES_KEY, JSON.stringify(updatedNotes));
    } catch (error) {
      console.error('Error updating offline note:', error);
    }
  },

  // Supprimer une note hors ligne
  deleteOfflineNote: async (noteId: string): Promise<void> => {
    try {
      const notes = await offlineStorage.getOfflineNotes();
      const updatedNotes = notes.filter(note => note.id !== noteId);
      await AsyncStorage.setItem(OFFLINE_NOTES_KEY, JSON.stringify(updatedNotes));
    } catch (error) {
      console.error('Error deleting offline note:', error);
    }
  },

  // Marquer une note pour synchronisation
  markForSync: async (noteId: string): Promise<void> => {
    try {
      const pendingSync = await AsyncStorage.getItem(PENDING_SYNC_KEY);
      const pendingIds = pendingSync ? JSON.parse(pendingSync) : [];
      if (!pendingIds.includes(noteId)) {
        pendingIds.push(noteId);
        await AsyncStorage.setItem(PENDING_SYNC_KEY, JSON.stringify(pendingIds));
      }
    } catch (error) {
      console.error('Error marking note for sync:', error);
    }
  },

  // Récupérer les notes en attente de synchronisation
  getPendingSyncNotes: async (): Promise<string[]> => {
    try {
      const pendingSync = await AsyncStorage.getItem(PENDING_SYNC_KEY);
      return pendingSync ? JSON.parse(pendingSync) : [];
    } catch (error) {
      console.error('Error getting pending sync notes:', error);
      return [];
    }
  },

  // Supprimer une note de la liste de synchronisation
  removeFromPendingSync: async (noteId: string): Promise<void> => {
    try {
      const pendingSync = await AsyncStorage.getItem(PENDING_SYNC_KEY);
      const pendingIds = pendingSync ? JSON.parse(pendingSync) : [];
      const updatedIds = pendingIds.filter((id: string) => id !== noteId);
      await AsyncStorage.setItem(PENDING_SYNC_KEY, JSON.stringify(updatedIds));
    } catch (error) {
      console.error('Error removing from pending sync:', error);
    }
  },

  // Nettoyer le stockage hors ligne
  clearOfflineStorage: async (): Promise<void> => {
    try {
      await AsyncStorage.multiRemove([OFFLINE_NOTES_KEY, PENDING_SYNC_KEY]);
    } catch (error) {
      console.error('Error clearing offline storage:', error);
    }
  },
};
