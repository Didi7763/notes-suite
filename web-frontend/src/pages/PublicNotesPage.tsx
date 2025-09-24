import React from 'react';
import { Container, Typography, Box } from '@mui/material';
import { Public } from '@mui/icons-material';

const PublicNotesPage: React.FC = () => {
  return (
    <Container maxWidth="xl">
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" gutterBottom>
          <Public sx={{ mr: 1, verticalAlign: 'middle' }} />
          Notes publiques
        </Typography>
      </Box>
      
      <Box sx={{ textAlign: 'center', py: 8 }}>
        <Public sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
        <Typography variant="h6" color="text.secondary">
          Page en cours de développement
        </Typography>
      </Box>
    </Container>
  );
};

export default PublicNotesPage;


