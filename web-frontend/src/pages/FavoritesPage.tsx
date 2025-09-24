import React from 'react';
import { Container, Typography, Box } from '@mui/material';
import { Favorite } from '@mui/icons-material';

const FavoritesPage: React.FC = () => {
  return (
    <Container maxWidth="xl">
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" gutterBottom>
          <Favorite sx={{ mr: 1, verticalAlign: 'middle' }} />
          Notes favorites
        </Typography>
      </Box>
      
      <Box sx={{ textAlign: 'center', py: 8 }}>
        <Favorite sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
        <Typography variant="h6" color="text.secondary">
          Page en cours de développement
        </Typography>
      </Box>
    </Container>
  );
};

export default FavoritesPage;


