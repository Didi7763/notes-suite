import React from 'react';
import { View, StyleSheet, ScrollView } from 'react-native';
import {
  Text,
  Card,
  Title,
  Paragraph,
  Chip,
  Button,
  Divider,
} from 'react-native-paper';
import { useQuery } from 'react-query';
import { useNavigation, useRoute } from '@react-navigation/native';
import { format } from 'date-fns';
import { fr } from 'date-fns/locale';
import { noteService } from '../services/api';
import { offlineStorage } from '../services/offlineStorage';
import { useOffline } from '../hooks/useOffline';
import { Note, OfflineNote } from '../types';

const NoteViewerScreen: React.FC = () => {
  const navigation = useNavigation();
  const route = useRoute();
  const { isOfflineMode } = useOffline();
  const noteId = route.params?.noteId;

  const { data: note, isLoading } = useQuery(
    ['note', noteId],
    () => noteService.getNote(noteId),
    {
      enabled: !isOfflineMode,
    }
  );

  const [offlineNote, setOfflineNote] = React.useState<OfflineNote | null>(null);

  React.useEffect(() => {
    if (isOfflineMode) {
      loadOfflineNote();
    }
  }, [isOfflineMode, noteId]);

  const loadOfflineNote = async () => {
    const notes = await offlineStorage.getOfflineNotes();
    const found = notes.find(n => n.id === noteId.toString());
    setOfflineNote(found || null);
  };

  const currentNote = isOfflineMode ? offlineNote : note;

  if (isLoading) {
    return (
      <View style={styles.centerContainer}>
        <Text>Chargement...</Text>
      </View>
    );
  }

  if (!currentNote) {
    return (
      <View style={styles.centerContainer}>
        <Text>Note non trouvée</Text>
        <Button
          mode="contained"
          onPress={() => navigation.goBack()}
          style={styles.backButton}
        >
          Retour
        </Button>
      </View>
    );
  }

  return (
    <ScrollView style={styles.container}>
      <View style={styles.content}>
        <Card style={styles.card}>
          <Card.Content>
            <View style={styles.header}>
              <Title style={styles.title}>{currentNote.title}</Title>
              {currentNote.isPublic && (
                <Chip icon="earth" mode="outlined">
                  Public
                </Chip>
              )}
            </View>
            
            <Divider style={styles.divider} />
            
            <Paragraph style={styles.content}>
              {currentNote.content}
            </Paragraph>
            
            <Divider style={styles.divider} />
            
            <View style={styles.metadata}>
              <Text style={styles.metadataText}>
                Créé le {format(new Date(currentNote.createdAt), 'dd MMMM yyyy à HH:mm', { locale: fr })}
              </Text>
              
              {currentNote.updatedAt !== currentNote.createdAt && (
                <Text style={styles.metadataText}>
                  Modifié le {format(new Date(currentNote.updatedAt), 'dd MMMM yyyy à HH:mm', { locale: fr })}
                </Text>
              )}
              
              {!isOfflineMode && 'user' in currentNote && (
                <Text style={styles.metadataText}>
                  Par {currentNote.user.username}
                </Text>
              )}
              
              {isOfflineMode && 'syncStatus' in currentNote && (
                <Chip
                  icon={currentNote.syncStatus === 'pending' ? 'sync' : 'error'}
                  mode="outlined"
                  style={styles.syncChip}
                >
                  {currentNote.syncStatus === 'pending' ? 'En attente de sync' : 'Erreur de sync'}
                </Chip>
              )}
            </View>
          </Card.Content>
          
          <Card.Actions>
            <Button
              mode="contained"
              onPress={() => navigation.navigate('NoteEditor' as never, { noteId: currentNote.id } as never)}
            >
              Modifier
            </Button>
            <Button
              mode="outlined"
              onPress={() => navigation.goBack()}
            >
              Retour
            </Button>
          </Card.Actions>
        </Card>
      </View>
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
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  title: {
    flex: 1,
    fontSize: 24,
    fontWeight: 'bold',
  },
  divider: {
    marginVertical: 16,
  },
  content: {
    fontSize: 16,
    lineHeight: 24,
    color: '#333',
  },
  metadata: {
    marginTop: 16,
  },
  metadataText: {
    fontSize: 12,
    color: '#666',
    marginBottom: 4,
  },
  syncChip: {
    marginTop: 8,
    alignSelf: 'flex-start',
  },
  centerContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  backButton: {
    marginTop: 16,
  },
});

export default NoteViewerScreen;
