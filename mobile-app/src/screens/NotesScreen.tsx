import React, { useState } from 'react';
import { View, StyleSheet, FlatList, Alert } from 'react-native';
import {
  Text,
  Card,
  Title,
  Paragraph,
  Button,
  FAB,
  Searchbar,
  Chip,
  IconButton,
  Menu,
  Divider,
} from 'react-native-paper';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { useNavigation } from '@react-navigation/native';
import { format } from 'date-fns';
import { fr } from 'date-fns/locale';
import { noteService } from '../services/api';
import { offlineStorage } from '../services/offlineStorage';
import { useOffline } from '../hooks/useOffline';
import { Note, OfflineNote } from '../types';

const NotesScreen: React.FC = () => {
  const navigation = useNavigation();
  const queryClient = useQueryClient();
  const { isOfflineMode } = useOffline();
  const [searchQuery, setSearchQuery] = useState('');
  const [offlineNotes, setOfflineNotes] = useState<OfflineNote[]>([]);

  const { data: notes = [], isLoading } = useQuery(
    'notes',
    noteService.getNotes,
    {
      enabled: !isOfflineMode,
    }
  );

  const deleteNoteMutation = useMutation(noteService.deleteNote, {
    onSuccess: () => {
      queryClient.invalidateQueries('notes');
    },
  });

  const loadOfflineNotes = async () => {
    if (isOfflineMode) {
      const offline = await offlineStorage.getOfflineNotes();
      setOfflineNotes(offline);
    }
  };

  React.useEffect(() => {
    loadOfflineNotes();
  }, [isOfflineMode]);

  const handleDeleteNote = (id: number) => {
    Alert.alert(
      'Supprimer la note',
      'Êtes-vous sûr de vouloir supprimer cette note ?',
      [
        { text: 'Annuler', style: 'cancel' },
        {
          text: 'Supprimer',
          style: 'destructive',
          onPress: () => deleteNoteMutation.mutate(id),
        },
      ]
    );
  };

  const filteredNotes = isOfflineMode
    ? offlineNotes.filter(note =>
        note.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        note.content.toLowerCase().includes(searchQuery.toLowerCase())
      )
    : notes.filter(note =>
        note.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        note.content.toLowerCase().includes(searchQuery.toLowerCase())
      );

  const renderNote = ({ item }: { item: Note | OfflineNote }) => (
    <Card style={styles.noteCard}>
      <Card.Content>
        <View style={styles.noteHeader}>
          <Title style={styles.noteTitle} numberOfLines={1}>
            {item.title}
          </Title>
          {item.isPublic && (
            <Chip icon="earth" mode="outlined" compact>
              Public
            </Chip>
          )}
        </View>
        
        <Paragraph style={styles.noteContent} numberOfLines={3}>
          {item.content}
        </Paragraph>
        
        <Text style={styles.noteDate}>
          {format(new Date(item.createdAt), 'dd MMMM yyyy', { locale: fr })}
        </Text>
        
        {isOfflineMode && 'syncStatus' in item && (
          <Chip
            icon={item.syncStatus === 'pending' ? 'sync' : 'error'}
            mode="outlined"
            compact
            style={styles.syncChip}
          >
            {item.syncStatus === 'pending' ? 'En attente' : 'Erreur'}
          </Chip>
        )}
      </Card.Content>
      
      <Card.Actions>
        <Button
          mode="text"
          onPress={() => navigation.navigate('NoteViewer' as never, { noteId: item.id } as never)}
        >
          Voir
        </Button>
        <Button
          mode="text"
          onPress={() => navigation.navigate('NoteEditor' as never, { noteId: item.id } as never)}
        >
          Modifier
        </Button>
        <IconButton
          icon="delete"
          size={20}
          onPress={() => handleDeleteNote(item.id)}
        />
      </Card.Actions>
    </Card>
  );

  if (isLoading) {
    return (
      <View style={styles.centerContainer}>
        <Text>Chargement...</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Searchbar
        placeholder="Rechercher dans mes notes..."
        onChangeText={setSearchQuery}
        value={searchQuery}
        style={styles.searchbar}
      />
      
      {isOfflineMode && (
        <View style={styles.offlineBanner}>
          <Text style={styles.offlineText}>
            Mode hors ligne - Seules les notes locales sont disponibles
          </Text>
        </View>
      )}
      
      {filteredNotes.length === 0 ? (
        <View style={styles.centerContainer}>
          <Text style={styles.emptyText}>
            {searchQuery ? 'Aucune note trouvée' : 'Aucune note'}
          </Text>
          <Button
            mode="contained"
            onPress={() => navigation.navigate('NoteEditor' as never)}
            style={styles.createButton}
          >
            Créer une note
          </Button>
        </View>
      ) : (
        <FlatList
          data={filteredNotes}
          renderItem={renderNote}
          keyExtractor={(item) => item.id.toString()}
          contentContainerStyle={styles.listContainer}
        />
      )}
      
      <FAB
        icon="plus"
        style={styles.fab}
        onPress={() => navigation.navigate('NoteEditor' as never)}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  searchbar: {
    margin: 16,
  },
  offlineBanner: {
    backgroundColor: '#ff9800',
    padding: 8,
    marginHorizontal: 16,
    borderRadius: 4,
  },
  offlineText: {
    color: 'white',
    textAlign: 'center',
    fontSize: 12,
  },
  listContainer: {
    padding: 16,
  },
  noteCard: {
    marginBottom: 16,
    elevation: 2,
  },
  noteHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  noteTitle: {
    flex: 1,
    fontSize: 18,
    fontWeight: 'bold',
  },
  noteContent: {
    marginBottom: 8,
    color: '#666',
  },
  noteDate: {
    fontSize: 12,
    color: '#999',
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
  emptyText: {
    fontSize: 16,
    color: '#666',
    marginBottom: 16,
    textAlign: 'center',
  },
  createButton: {
    marginTop: 8,
  },
  fab: {
    position: 'absolute',
    margin: 16,
    right: 0,
    bottom: 0,
  },
});

export default NotesScreen;
