import React, { useState } from 'react';
import { View, StyleSheet, FlatList } from 'react-native';
import {
  Text,
  Card,
  Title,
  Paragraph,
  Searchbar,
  Chip,
  Button,
} from 'react-native-paper';
import { useQuery } from 'react-query';
import { useNavigation } from '@react-navigation/native';
import { format } from 'date-fns';
import { fr } from 'date-fns/locale';
import { noteService } from '../services/api';
import { Note } from '../types';

const PublicNotesScreen: React.FC = () => {
  const navigation = useNavigation();
  const [searchQuery, setSearchQuery] = useState('');

  const { data: notes = [], isLoading } = useQuery(
    'public-notes',
    noteService.getPublicNotes
  );

  const filteredNotes = notes.filter((note: Note) =>
    note.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
    note.content.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const renderNote = ({ item }: { item: Note }) => (
    <Card style={styles.noteCard}>
      <Card.Content>
        <View style={styles.noteHeader}>
          <Title style={styles.noteTitle} numberOfLines={1}>
            {item.title}
          </Title>
          <Chip icon="earth" mode="outlined" compact>
            Public
          </Chip>
        </View>
        
        <Paragraph style={styles.noteContent} numberOfLines={3}>
          {item.content}
        </Paragraph>
        
        <Text style={styles.noteAuthor}>
          Par {item.user.username}
        </Text>
        
        <Text style={styles.noteDate}>
          {format(new Date(item.createdAt), 'dd MMMM yyyy', { locale: fr })}
        </Text>
      </Card.Content>
      
      <Card.Actions>
        <Button
          mode="text"
          onPress={() => navigation.navigate('NoteViewer' as never, { noteId: item.id } as never)}
        >
          Voir
        </Button>
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
        placeholder="Rechercher dans les notes publiques..."
        onChangeText={setSearchQuery}
        value={searchQuery}
        style={styles.searchbar}
      />
      
      {filteredNotes.length === 0 ? (
        <View style={styles.centerContainer}>
          <Text style={styles.emptyText}>
            {searchQuery ? 'Aucune note publique trouvée' : 'Aucune note publique'}
          </Text>
        </View>
      ) : (
        <FlatList
          data={filteredNotes}
          renderItem={renderNote}
          keyExtractor={(item) => item.id.toString()}
          contentContainerStyle={styles.listContainer}
        />
      )}
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
  noteAuthor: {
    fontSize: 12,
    color: '#1976d2',
    fontWeight: '500',
    marginBottom: 4,
  },
  noteDate: {
    fontSize: 12,
    color: '#999',
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
    textAlign: 'center',
  },
});

export default PublicNotesScreen;
