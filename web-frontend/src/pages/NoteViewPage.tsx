import React, { useEffect, useState } from 'react';
import {
  Container,
  Paper,
  Typography,
  Box,
  Chip,
  Button,
  IconButton,
  Divider,
} from '@mui/material';
import {
  Edit,
  Share,
  Favorite,
  FavoriteBorder,
  Visibility,
  AccessTime,
} from '@mui/icons-material';
import { useParams, useNavigate } from 'react-router-dom';
import ReactMarkdown from 'react-markdown';
import { useNotes } from '@/hooks/useNotes';
import { Note } from '@/types';
import { formatDateTime } from '@/utils/dateUtils';
import LoadingSpinner from '@/components/LoadingSpinner';

const NoteViewPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { notes, loadNotes, toggleFavorite } = useNotes();
  const [note, setNote] = useState<Note | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (id) {
      loadNotes().then(() => {
        const foundNote = notes.find(n => n.id === parseInt(id));
        setNote(foundNote || null);
        setIsLoading(false);
      });
    }
  }, [id, loadNotes, notes]);

  const handleToggleFavorite = async () => {
    if (note) {
      try {
        await toggleFavorite(note.id);
        setNote({ ...note, isFavorite: !note.isFavorite });
      } catch (error) {
        console.error('Erreur lors de la modification du favori:', error);
      }
    }
  };

  if (isLoading) {
    return <LoadingSpinner message="Chargement de la note..." />;
  }

  if (!note) {
    return (
      <Container maxWidth="md">
        <Box sx={{ textAlign: 'center', py: 8 }}>
          <Typography variant="h6" color="text.secondary">
            Note non trouvée
          </Typography>
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="md">
      <Paper elevation={3} sx={{ p: 4 }}>
        {/* En-tête */}
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 3 }}>
          <Box sx={{ flexGrow: 1 }}>
            <Typography variant="h4" gutterBottom>
              {note.title}
            </Typography>
            <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', mb: 2 }}>
              <Chip
                label={note.visibility}
                color={note.visibility === 'PUBLIC' ? 'success' : note.visibility === 'SHARED' ? 'warning' : 'default'}
                size="small"
              />
              <Typography variant="body2" color="text.secondary">
                <AccessTime sx={{ fontSize: 16, mr: 0.5, verticalAlign: 'middle' }} />
                {formatDateTime(note.updatedAt)}
              </Typography>
            </Box>
          </Box>
          
          <Box sx={{ display: 'flex', gap: 1 }}>
            <IconButton onClick={handleToggleFavorite}>
              {note.isFavorite ? <Favorite color="error" /> : <FavoriteBorder />}
            </IconButton>
            <IconButton>
              <Share />
            </IconButton>
            <Button
              variant="outlined"
              startIcon={<Edit />}
              onClick={() => navigate(`/notes/${note.id}/edit`)}
            >
              Modifier
            </Button>
          </Box>
        </Box>

        <Divider sx={{ mb: 3 }} />

        {/* Tags */}
        {note.tags.length > 0 && (
          <Box sx={{ mb: 3 }}>
            <Typography variant="subtitle2" gutterBottom>
              Tags:
            </Typography>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
              {note.tags.map((tag) => (
                <Chip
                  key={tag.id}
                  label={tag.label}
                  variant="outlined"
                  size="small"
                />
              ))}
            </Box>
          </Box>
        )}

        {/* Contenu */}
        <Box sx={{ mb: 3 }}>
          <Typography variant="subtitle2" gutterBottom>
            Contenu:
          </Typography>
          <Paper variant="outlined" sx={{ p: 3, backgroundColor: 'grey.50' }}>
            {note.contentMd ? (
              <ReactMarkdown>{note.contentMd}</ReactMarkdown>
            ) : (
              <Typography color="text.secondary" fontStyle="italic">
                Aucun contenu
              </Typography>
            )}
          </Paper>
        </Box>

        {/* Métadonnées */}
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', pt: 2, borderTop: 1, borderColor: 'divider' }}>
          <Typography variant="body2" color="text.secondary">
            Créée le {formatDateTime(note.createdAt)}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            <Visibility sx={{ fontSize: 16, mr: 0.5, verticalAlign: 'middle' }} />
            {note.viewCount} vue{note.viewCount > 1 ? 's' : ''}
          </Typography>
        </Box>
      </Paper>
    </Container>
  );
};

export default NoteViewPage;


