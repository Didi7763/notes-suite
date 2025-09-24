import React, { useState, useEffect } from 'react';
import { View, StyleSheet, ScrollView, Alert } from 'react-native';
import {
  Text,
  TextInput,
  Button,
  Card,
  Title,
  Switch,
  Menu,
  Divider,
  Snackbar,
} from 'react-native-paper';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { useNavigation, useRoute } from '@react-navigation/native';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { noteService } from '../services/api';
import { offlineStorage } from '../services/offlineStorage';
import { useOffline } from '../hooks/useOffline';
import { Note, OfflineNote } from '../types';
import { v4 as uuidv4 } from 'react-native-uuid';

const schema = yup.object({
  title: yup.string().required('Le titre est requis'),
  content: yup.string().required('Le contenu est requis'),
});

interface NoteFormData {
  title: string;
  content: string;
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  isPublic: boolean;
}

const NoteEditorScreen: React.FC = () => {
  const navigation = useNavigation();
  const route = useRoute();
  const queryClient = useQueryClient();
  const { isOfflineMode } = useOffline();
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  
  const noteId = route.params?.noteId;
  const isEditing = Boolean(noteId);

  const {
    control,
    handleSubmit,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<NoteFormData>({
    resolver: yupResolver(schema),
    defaultValues: {
      title: '',
      content: '',
      status: 'DRAFT',
      isPublic: false,
    },
  });

  const watchedValues = watch();

  const { data: note, isLoading } = useQuery(
    ['note', noteId],
    () => noteService.getNote(noteId),
    {
      enabled: isEditing && !isOfflineMode,
      onSuccess: (data) => {
        setValue('title', data.title);
        setValue('content', data.content);
        setValue('status', data.status);
        setValue('isPublic', data.isPublic);
      },
    }
  );

  const createMutation = useMutation(noteService.createNote, {
    onSuccess: () => {
      queryClient.invalidateQueries('notes');
      setSuccess('Note créée avec succès !');
      setTimeout(() => navigation.goBack(), 1500);
    },
    onError: (error: any) => {
      setError(error.response?.data?.message || 'Erreur lors de la création');
    },
  });

  const updateMutation = useMutation(
    (data: Partial<Note>) => noteService.updateNote(noteId, data),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('notes');
        queryClient.invalidateQueries(['note', noteId]);
        setSuccess('Note mise à jour avec succès !');
        setTimeout(() => navigation.goBack(), 1500);
      },
      onError: (error: any) => {
        setError(error.response?.data?.message || 'Erreur lors de la mise à jour');
      },
    }
  );

  const saveOfflineNote = async (data: NoteFormData) => {
    const offlineNote: OfflineNote = {
      id: noteId ? noteId.toString() : uuidv4(),
      title: data.title,
      content: data.content,
      status: data.status,
      isPublic: data.isPublic,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      isOffline: true,
      syncStatus: 'pending',
    };

    if (isEditing) {
      await offlineStorage.updateOfflineNote(offlineNote.id, offlineNote);
    } else {
      await offlineStorage.saveOfflineNote(offlineNote);
    }

    setSuccess('Note sauvegardée hors ligne !');
    setTimeout(() => navigation.goBack(), 1500);
  };

  const onSubmit = async (data: NoteFormData) => {
    try {
      setError('');
      setSuccess('');
      
      if (isOfflineMode) {
        await saveOfflineNote(data);
      } else {
        if (isEditing) {
          updateMutation.mutate(data);
        } else {
          createMutation.mutate(data);
        }
      }
    } catch (err) {
      console.error('Error:', err);
    }
  };

  if (isLoading) {
    return (
      <View style={styles.centerContainer}>
        <Text>Chargement...</Text>
      </View>
    );
  }

  return (
    <ScrollView style={styles.container}>
      <View style={styles.content}>
        <Card style={styles.card}>
          <Card.Content>
            <Title style={styles.title}>
              {isEditing ? 'Modifier la note' : 'Nouvelle note'}
            </Title>
            
            {isOfflineMode && (
              <Text style={styles.offlineText}>
                Mode hors ligne - La note sera synchronisée lors de la reconnexion
              </Text>
            )}
            
            <Controller
              control={control}
              name="title"
              render={({ field: { onChange, onBlur, value } }) => (
                <TextInput
                  label="Titre"
                  mode="outlined"
                  value={value}
                  onBlur={onBlur}
                  onChangeText={onChange}
                  error={!!errors.title}
                  style={styles.input}
                />
              )}
            />
            {errors.title && (
              <Text style={styles.errorText}>{errors.title.message}</Text>
            )}
            
            <Controller
              control={control}
              name="content"
              render={({ field: { onChange, onBlur, value } }) => (
                <TextInput
                  label="Contenu"
                  mode="outlined"
                  multiline
                  numberOfLines={12}
                  value={value}
                  onBlur={onBlur}
                  onChangeText={onChange}
                  error={!!errors.content}
                  style={styles.input}
                />
              )}
            />
            {errors.content && (
              <Text style={styles.errorText}>{errors.content.message}</Text>
            )}
            
            <View style={styles.switchContainer}>
              <Text>Note publique</Text>
              <Controller
                control={control}
                name="isPublic"
                render={({ field: { onChange, value } }) => (
                  <Switch
                    value={value}
                    onValueChange={onChange}
                  />
                )}
              />
            </View>
            
            <Button
              mode="contained"
              onPress={handleSubmit(onSubmit)}
              loading={isSubmitting || createMutation.isLoading || updateMutation.isLoading}
              disabled={isSubmitting || createMutation.isLoading || updateMutation.isLoading}
              style={styles.saveButton}
            >
              {isSubmitting || createMutation.isLoading || updateMutation.isLoading
                ? 'Sauvegarde...'
                : isEditing
                ? 'Mettre à jour'
                : 'Créer'}
            </Button>
            
            <Button
              mode="outlined"
              onPress={() => navigation.goBack()}
              style={styles.cancelButton}
            >
              Annuler
            </Button>
          </Card.Content>
        </Card>
      </View>

      <Snackbar
        visible={!!error}
        onDismiss={() => setError('')}
        duration={4000}
      >
        {error}
      </Snackbar>

      <Snackbar
        visible={!!success}
        onDismiss={() => setSuccess('')}
        duration={4000}
      >
        {success}
      </Snackbar>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  content: {
    padding: 20,
  },
  card: {
    elevation: 4,
  },
  title: {
    textAlign: 'center',
    marginBottom: 16,
    fontSize: 24,
    fontWeight: 'bold',
  },
  offlineText: {
    color: '#ff9800',
    textAlign: 'center',
    marginBottom: 16,
    fontSize: 12,
  },
  input: {
    marginBottom: 16,
  },
  errorText: {
    color: 'red',
    fontSize: 12,
    marginBottom: 16,
    marginLeft: 12,
  },
  switchContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 24,
  },
  saveButton: {
    marginBottom: 8,
  },
  cancelButton: {
    marginBottom: 16,
  },
  centerContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
});

export default NoteEditorScreen;
