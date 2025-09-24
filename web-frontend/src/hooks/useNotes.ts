import { useState, useEffect, useCallback } from 'react';
import apiService from '@/services/api';
import { Note, NoteCreateRequest, NoteUpdateRequest, NoteFilters, ApiResponse } from '@/types';

interface UseNotesReturn {
  notes: Note[];
  totalCount: number;
  isLoading: boolean;
  error: string | null;
  currentPage: number;
  totalPages: number;
  loadNotes: (filters?: NoteFilters) => Promise<void>;
  createNote: (noteData: NoteCreateRequest) => Promise<Note>;
  updateNote: (id: number, noteData: NoteUpdateRequest) => Promise<Note>;
  deleteNote: (id: number) => Promise<void>;
  toggleFavorite: (id: number) => Promise<void>;
  clearError: () => void;
}

export const useNotes = (): UseNotesReturn => {
  const [notes, setNotes] = useState<Note[]>([]);
  const [totalCount, setTotalCount] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadNotes = useCallback(async (filters: NoteFilters = {}) => {
    try {
      setIsLoading(true);
      setError(null);
      
      const response: ApiResponse<Note> = await apiService.getNotes(filters);
      
      setNotes(response.content);
      setTotalCount(response.totalElements);
      setCurrentPage(response.currentPage);
      setTotalPages(response.totalPages);
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 'Erreur lors du chargement des notes';
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const createNote = useCallback(async (noteData: NoteCreateRequest): Promise<Note> => {
    try {
      setError(null);
      const newNote = await apiService.createNote(noteData);
      
      // Recharger les notes pour inclure la nouvelle
      await loadNotes();
      
      return newNote;
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 'Erreur lors de la création de la note';
      setError(errorMessage);
      throw error;
    }
  }, [loadNotes]);

  const updateNote = useCallback(async (id: number, noteData: NoteUpdateRequest): Promise<Note> => {
    try {
      setError(null);
      const updatedNote = await apiService.updateNote(id, noteData);
      
      // Mettre à jour la note dans la liste
      setNotes(prevNotes => 
        prevNotes.map(note => 
          note.id === id ? updatedNote : note
        )
      );
      
      return updatedNote;
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 'Erreur lors de la mise à jour de la note';
      setError(errorMessage);
      throw error;
    }
  }, []);

  const deleteNote = useCallback(async (id: number): Promise<void> => {
    try {
      setError(null);
      await apiService.deleteNote(id);
      
      // Supprimer la note de la liste
      setNotes(prevNotes => prevNotes.filter(note => note.id !== id));
      setTotalCount(prev => prev - 1);
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 'Erreur lors de la suppression de la note';
      setError(errorMessage);
      throw error;
    }
  }, []);

  const toggleFavorite = useCallback(async (id: number): Promise<void> => {
    try {
      setError(null);
      const updatedNote = await apiService.toggleFavorite(id);
      
      // Mettre à jour la note dans la liste
      setNotes(prevNotes => 
        prevNotes.map(note => 
          note.id === id ? updatedNote : note
        )
      );
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 'Erreur lors de la modification du favori';
      setError(errorMessage);
      throw error;
    }
  }, []);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  return {
    notes,
    totalCount,
    isLoading,
    error,
    currentPage,
    totalPages,
    loadNotes,
    createNote,
    updateNote,
    deleteNote,
    toggleFavorite,
    clearError,
  };
};


