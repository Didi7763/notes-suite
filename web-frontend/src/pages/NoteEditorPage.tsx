import React, { useState, useEffect } from 'react';
import {
  Container,
  Paper,
  TextField,
  Button,
  Typography,
  Box,
  Chip,
  Autocomplete,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  CircularProgress,
} from '@mui/material';
import { Save, Cancel } from '@mui/icons-material';
import { useForm, Controller } from 'react-hook-form';
import { useNavigate, useParams } from 'react-router-dom';
import { useNotes } from '@/hooks/useNotes';
import { NoteCreateRequest, NoteUpdateRequest, NoteVisibility } from '@/types';
import { getRequiredMessage, getMaxLengthMessage } from '@/utils/validation';

const NoteEditorPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;
  const { createNote, updateNote, notes, loadNotes } = useNotes();
  
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [availableTags, setAvailableTags] = useState<string[]>([]);

  const {
    register,
    handleSubmit,
    control,
    setValue,
    watch,
    formState: { errors },
  } = useForm<NoteCreateRequest & NoteUpdateRequest>();

  const selectedTags = watch('tags') || [];

  useEffect(() => {
    // Charger les tags disponibles depuis les notes existantes
    const tags = new Set<string>();
    notes.forEach(note => {
      note.tags.forEach(tag => tags.add(tag.label));
    });
    setAvailableTags(Array.from(tags));
  }, [notes]);

  useEffect(() => {
    if (isEditing && id) {
      // Charger la note à éditer
      loadNotes().then(() => {
        const note = notes.find(n => n.id === parseInt(id));
        if (note) {
          setValue('title', note.title);
          setValue('contentMd', note.contentMd || '');
          setValue('visibility', note.visibility);
          setValue('tags', note.tags.map(tag => tag.label));
        }
      });
    }
  }, [isEditing, id, setValue, loadNotes, notes]);

  const onSubmit = async (data: NoteCreateRequest & NoteUpdateRequest) => {
    try {
      setIsLoading(true);
      setError(null);

      if (isEditing && id) {
        await updateNote(parseInt(id), data);
      } else {
        await createNote(data);
      }

      navigate('/notes');
    } catch (error: any) {
      setError(error.response?.data?.message || 'Une erreur est survenue');
    } finally {
      setIsLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/notes');
  };

  return (
    <Container maxWidth="md">
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" gutterBottom>
          {isEditing ? 'Modifier la note' : 'Nouvelle note'}
        </Typography>
      </Box>

      <Paper elevation={3} sx={{ p: 4 }}>
        {error && (
          <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        <Box component="form" onSubmit={handleSubmit(onSubmit)}>
          {/* Titre */}
          <TextField
            fullWidth
            label="Titre"
            margin="normal"
            {...register('title', {
              required: getRequiredMessage('Le titre'),
              maxLength: {
                value: 255,
                message: getMaxLengthMessage('Le titre', 255),
              },
            })}
            error={!!errors.title}
            helperText={errors.title?.message}
          />

          {/* Visibilité */}
          <FormControl fullWidth margin="normal">
            <InputLabel>Visibilité</InputLabel>
            <Controller
              name="visibility"
              control={control}
              defaultValue={NoteVisibility.PRIVATE}
              rules={{ required: getRequiredMessage('La visibilité') }}
              render={({ field }) => (
                <Select
                  {...field}
                  label="Visibilité"
                  error={!!errors.visibility}
                >
                  <MenuItem value={NoteVisibility.PRIVATE}>Privée</MenuItem>
                  <MenuItem value={NoteVisibility.SHARED}>Partagée</MenuItem>
                  <MenuItem value={NoteVisibility.PUBLIC}>Publique</MenuItem>
                </Select>
              )}
            />
          </FormControl>

          {/* Tags */}
          <Autocomplete
            multiple
            freeSolo
            options={availableTags}
            value={selectedTags}
            onChange={(_, newValue) => setValue('tags', newValue)}
            renderTags={(value, getTagProps) =>
              value.map((option, index) => (
                <Chip
                  variant="outlined"
                  label={option}
                  {...getTagProps({ index })}
                  key={option}
                />
              ))
            }
            renderInput={(params) => (
              <TextField
                {...params}
                label="Tags"
                margin="normal"
                placeholder="Ajouter des tags..."
                helperText="Appuyez sur Entrée pour ajouter un tag"
              />
            )}
          />

          {/* Contenu */}
          <TextField
            fullWidth
            label="Contenu (Markdown)"
            multiline
            rows={15}
            margin="normal"
            {...register('contentMd')}
            placeholder="Écrivez votre note en Markdown..."
            helperText="Vous pouvez utiliser la syntaxe Markdown pour formater votre texte"
          />

          {/* Boutons d'action */}
          <Box sx={{ display: 'flex', gap: 2, mt: 4, justifyContent: 'flex-end' }}>
            <Button
              variant="outlined"
              startIcon={<Cancel />}
              onClick={handleCancel}
              disabled={isLoading}
            >
              Annuler
            </Button>
            <Button
              type="submit"
              variant="contained"
              startIcon={isLoading ? <CircularProgress size={20} /> : <Save />}
              disabled={isLoading}
            >
              {isLoading ? 'Sauvegarde...' : isEditing ? 'Mettre à jour' : 'Créer'}
            </Button>
          </Box>
        </Box>
      </Paper>
    </Container>
  );
};

export default NoteEditorPage;


