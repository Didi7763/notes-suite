import React, { useEffect, useState } from 'react';
import {
  Container,
  Typography,
  Box,
  Button,
  TextField,
  Grid,
  Card,
  CardContent,
  Chip,
  IconButton,
  InputAdornment,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Fab,
} from '@mui/material';
import {
  Add,
  Search,
  FilterList,
  Visibility,
  VisibilityOff,
  Favorite,
  FavoriteBorder,
  MoreVert,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useNotes } from '@/hooks/useNotes';
import { Note, NoteVisibility } from '@/types';
import { formatRelativeTime } from '@/utils/dateUtils';
import LoadingSpinner from '@/components/LoadingSpinner';

const NotesPage: React.FC = () => {
  const navigate = useNavigate();
  const { notes, totalCount, isLoading, loadNotes, toggleFavorite } = useNotes();
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedVisibility, setSelectedVisibility] = useState<NoteVisibility | ''>('');
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(20);

  useEffect(() => {
    loadNotes({
      query: searchQuery || undefined,
      visibility: selectedVisibility || undefined,
      page: currentPage,
      size: pageSize,
      sortBy: 'updatedAt',
      sortDir: 'desc',
    });
  }, [searchQuery, selectedVisibility, currentPage, pageSize, loadNotes]);

  const handleSearch = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSearchQuery(event.target.value);
    setCurrentPage(0);
  };

  const handleVisibilityChange = (event: any) => {
    setSelectedVisibility(event.target.value);
    setCurrentPage(0);
  };

  const handleToggleFavorite = async (noteId: number) => {
    try {
      await toggleFavorite(noteId);
    } catch (error) {
      console.error('Erreur lors de la modification du favori:', error);
    }
  };

  const getVisibilityColor = (visibility: NoteVisibility) => {
    switch (visibility) {
      case NoteVisibility.PUBLIC:
        return 'success';
      case NoteVisibility.SHARED:
        return 'warning';
      case NoteVisibility.PRIVATE:
        return 'default';
      default:
        return 'default';
    }
  };

  const getVisibilityLabel = (visibility: NoteVisibility) => {
    switch (visibility) {
      case NoteVisibility.PUBLIC:
        return 'Public';
      case NoteVisibility.SHARED:
        return 'Partagé';
      case NoteVisibility.PRIVATE:
        return 'Privé';
      default:
        return 'Inconnu';
    }
  };

  if (isLoading) {
    return <LoadingSpinner message="Chargement des notes..." />;
  }

  return (
    <Container maxWidth="xl">
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" gutterBottom>
          Mes notes
        </Typography>
        
        {/* Barre de recherche et filtres */}
        <Box sx={{ display: 'flex', gap: 2, mb: 3, flexWrap: 'wrap' }}>
          <TextField
            placeholder="Rechercher dans les notes..."
            value={searchQuery}
            onChange={handleSearch}
            sx={{ flexGrow: 1, minWidth: 300 }}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <Search />
                </InputAdornment>
              ),
            }}
          />
          
          <FormControl sx={{ minWidth: 150 }}>
            <InputLabel>Visibilité</InputLabel>
            <Select
              value={selectedVisibility}
              onChange={handleVisibilityChange}
              label="Visibilité"
            >
              <MenuItem value="">Toutes</MenuItem>
              <MenuItem value={NoteVisibility.PRIVATE}>Privées</MenuItem>
              <MenuItem value={NoteVisibility.SHARED}>Partagées</MenuItem>
              <MenuItem value={NoteVisibility.PUBLIC}>Publiques</MenuItem>
            </Select>
          </FormControl>
          
          <Button
            variant="outlined"
            startIcon={<Add />}
            onClick={() => navigate('/notes/create')}
          >
            Nouvelle note
          </Button>
        </Box>

        {/* Résultats */}
        <Box sx={{ mb: 2 }}>
          <Typography variant="body2" color="text.secondary">
            {totalCount} note{totalCount > 1 ? 's' : ''} trouvée{totalCount > 1 ? 's' : ''}
          </Typography>
        </Box>
      </Box>

      {/* Liste des notes */}
      {notes.length === 0 ? (
        <Box sx={{ textAlign: 'center', py: 8 }}>
          <Typography variant="h6" color="text.secondary" gutterBottom>
            Aucune note trouvée
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            {searchQuery || selectedVisibility
              ? 'Essayez de modifier vos critères de recherche'
              : 'Commencez par créer votre première note'}
          </Typography>
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => navigate('/notes/create')}
          >
            Créer une note
          </Button>
        </Box>
      ) : (
        <Grid container spacing={3}>
          {notes.map((note) => (
            <Grid item xs={12} sm={6} md={4} key={note.id}>
              <Card
                sx={{
                  height: '100%',
                  display: 'flex',
                  flexDirection: 'column',
                  cursor: 'pointer',
                  '&:hover': {
                    boxShadow: 4,
                  },
                }}
                onClick={() => navigate(`/notes/${note.id}`)}
              >
                <CardContent sx={{ flexGrow: 1 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                    <Typography variant="h6" component="h2" sx={{ fontWeight: 'bold' }}>
                      {note.title}
                    </Typography>
                    <Box sx={{ display: 'flex', gap: 0.5 }}>
                      <IconButton
                        size="small"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleToggleFavorite(note.id);
                        }}
                      >
                        {note.isFavorite ? <Favorite color="error" /> : <FavoriteBorder />}
                      </IconButton>
                      <IconButton size="small">
                        <MoreVert />
                      </IconButton>
                    </Box>
                  </Box>

                  <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{
                      mb: 2,
                      display: '-webkit-box',
                      WebkitLineClamp: 3,
                      WebkitBoxOrient: 'vertical',
                      overflow: 'hidden',
                    }}
                  >
                    {note.contentMd || 'Aucun contenu'}
                  </Typography>

                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                    <Chip
                      label={getVisibilityLabel(note.visibility)}
                      color={getVisibilityColor(note.visibility)}
                      size="small"
                    />
                    <Typography variant="caption" color="text.secondary">
                      {formatRelativeTime(note.updatedAt)}
                    </Typography>
                  </Box>

                  {note.tags.length > 0 && (
                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5, mt: 1 }}>
                      {note.tags.slice(0, 3).map((tag) => (
                        <Chip
                          key={tag.id}
                          label={tag.label}
                          size="small"
                          variant="outlined"
                        />
                      ))}
                      {note.tags.length > 3 && (
                        <Typography variant="caption" color="text.secondary">
                          +{note.tags.length - 3}
                        </Typography>
                      )}
                    </Box>
                  )}
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      {/* Bouton flottant pour créer une note */}
      <Fab
        color="primary"
        aria-label="add"
        sx={{
          position: 'fixed',
          bottom: 16,
          right: 16,
        }}
        onClick={() => navigate('/notes/create')}
      >
        <Add />
      </Fab>
    </Container>
  );
};

export default NotesPage;


