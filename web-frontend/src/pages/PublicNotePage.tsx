import React from 'react';
import { Container, Typography, Box } from '@mui/material';
import { useParams } from 'react-router-dom';

const PublicNotePage: React.FC = () => {
  const { token } = useParams<{ token: string }>();

  return (
    <Container maxWidth="md">
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" gutterBottom>
          Note publique
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Token: {token}
        </Typography>
      </Box>
      
      <Box sx={{ textAlign: 'center', py: 8 }}>
        <Typography variant="h6" color="text.secondary">
          Page en cours de développement
        </Typography>
      </Box>
    </Container>
  );
};

export default PublicNotePage;


