import React, { useEffect, useState } from 'react';
import {
  Container,
  Grid,
  Card,
  CardContent,
  Typography,
  Box,
  Button,
  Chip,
  IconButton,
  Fab,
} from '@mui/material';
import {
  Add,
  Note,
  Favorite,
  Share,
  Public,
  TrendingUp,
  AccessTime,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useNotes } from '@/hooks/useNotes';
import { Note as NoteType, NoteVisibility } from '@/types';
import { formatRelativeTime } from '@/utils/dateUtils';
import LoadingSpinner from '@/components/LoadingSpinner';

const DashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { notes, totalCount, isLoading, loadNotes } = useNotes();
  const [recentNotes, setRecentNotes] = useState<NoteType[]>([]);
  const [favoriteNotes, setFavoriteNotes] = useState<NoteType[]>([]);

  useEffect(() => {
    // Charger les notes récentes
    loadNotes({ page: 0, size: 5, sortBy: 'updatedAt', sortDir: 'desc' });
  }, [loadNotes]);

  useEffect(() => {
    // Filtrer les notes récentes et favorites
    setRecentNotes(notes.slice(0, 5));
    setFavoriteNotes(notes.filter(note => note.isFavorite).slice(0, 3));
  }, [notes]);

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
    return <LoadingSpinner message="Chargement du tableau de bord..." />;
  }

  return (
    <Container maxWidth="xl">
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" gutterBottom>
          Tableau de bord
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Bonjour {user?.email}, voici un aperçu de vos notes.
        </Typography>
      </Box>

      <Grid container spacing={3}>
        {/* Statistiques */}
        <Grid item xs={12} md={3}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <Note color="primary" sx={{ mr: 1 }} />
                <Typography variant="h6">Total des notes</Typography>
              </Box>
              <Typography variant="h3" color="primary">
                {totalCount}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={3}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <Favorite color="error" sx={{ mr: 1 }} />
                <Typography variant="h6">Favoris</Typography>
              </Box>
              <Typography variant="h3" color="error">
                {favoriteNotes.length}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={3}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <Share color="warning" sx={{ mr: 1 }} />
                <Typography variant="h6">Partagées</Typography>
              </Box>
              <Typography variant="h3" color="warning.main">
                {notes.filter(note => note.visibility === NoteVisibility.SHARED).length}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={3}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <Public color="success" sx={{ mr: 1 }} />
                <Typography variant="h6">Publiques</Typography>
              </Box>
              <Typography variant="h3" color="success.main">
                {notes.filter(note => note.visibility === NoteVisibility.PUBLIC).length}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        {/* Notes récentes */}
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h6">
                  <AccessTime sx={{ mr: 1, verticalAlign: 'middle' }} />
                  Notes récentes
                </Typography>
                <Button
                  variant="outlined"
                  size="small"
                  onClick={() => navigate('/notes')}
                >
                  Voir tout
                </Button>
              </Box>
              
              {recentNotes.length === 0 ? (
                <Box sx={{ textAlign: 'center', py: 4 }}>
                  <Note sx={{ fontSize: 48, color: 'text.secondary', mb: 2 }} />
                  <Typography variant="body1" color="text.secondary">
                    Aucune note pour le moment
                  </Typography>
                  <Button
                    variant="contained"
                    startIcon={<Add />}
                    onClick={() => navigate('/notes/create')}
                    sx={{ mt: 2 }}
                  >
                    Créer ma première note
                  </Button>
                </Box>
              ) : (
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                  {recentNotes.map((note) => (
                    <Box
                      key={note.id}
                      sx={{
                        p: 2,
                        border: '1px solid',
                        borderColor: 'divider',
                        borderRadius: 1,
                        cursor: 'pointer',
                        '&:hover': {
                          backgroundColor: 'action.hover',
                        },
                      }}
                      onClick={() => navigate(`/notes/${note.id}`)}
                    >
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                        <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }}>
                          {note.title}
                        </Typography>
                        <Chip
                          label={getVisibilityLabel(note.visibility)}
                          color={getVisibilityColor(note.visibility)}
                          size="small"
                        />
                      </Box>
                      <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                        {note.contentMd?.substring(0, 100)}
                        {note.contentMd && note.contentMd.length > 100 && '...'}
                      </Typography>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Typography variant="caption" color="text.secondary">
                          {formatRelativeTime(note.updatedAt)}
                        </Typography>
                        <Box sx={{ display: 'flex', gap: 1 }}>
                          {note.tags.slice(0, 2).map((tag) => (
                            <Chip
                              key={tag.id}
                              label={tag.label}
                              size="small"
                              variant="outlined"
                            />
                          ))}
                          {note.tags.length > 2 && (
                            <Typography variant="caption" color="text.secondary">
                              +{note.tags.length - 2}
                            </Typography>
                          )}
                        </Box>
                      </Box>
                    </Box>
                  ))}
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Notes favorites */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h6">
                  <Favorite sx={{ mr: 1, verticalAlign: 'middle' }} />
                  Favoris
                </Typography>
                <Button
                  variant="outlined"
                  size="small"
                  onClick={() => navigate('/favorites')}
                >
                  Voir tout
                </Button>
              </Box>
              
              {favoriteNotes.length === 0 ? (
                <Box sx={{ textAlign: 'center', py: 2 }}>
                  <Favorite sx={{ fontSize: 32, color: 'text.secondary', mb: 1 }} />
                  <Typography variant="body2" color="text.secondary">
                    Aucun favori
                  </Typography>
                </Box>
              ) : (
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                  {favoriteNotes.map((note) => (
                    <Box
                      key={note.id}
                      sx={{
                        p: 1.5,
                        border: '1px solid',
                        borderColor: 'divider',
                        borderRadius: 1,
                        cursor: 'pointer',
                        '&:hover': {
                          backgroundColor: 'action.hover',
                        },
                      }}
                      onClick={() => navigate(`/notes/${note.id}`)}
                    >
                      <Typography variant="subtitle2" sx={{ fontWeight: 'bold', mb: 0.5 }}>
                        {note.title}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {formatRelativeTime(note.updatedAt)}
                      </Typography>
                    </Box>
                  ))}
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

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

export default DashboardPage;

