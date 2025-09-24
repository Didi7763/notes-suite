import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Box } from '@mui/material';
import { useAuth } from '@/hooks/useAuth';

// Composants de layout
import Navbar from '@/components/Navbar';
import LoadingSpinner from '@/components/LoadingSpinner';

// Pages
import LoginPage from '@/pages/LoginPage';
import RegisterPage from '@/pages/RegisterPage';
import DashboardPage from '@/pages/DashboardPage';
import NotesPage from '@/pages/NotesPage';
import NoteEditorPage from '@/pages/NoteEditorPage';
import NoteViewPage from '@/pages/NoteViewPage';
import FavoritesPage from '@/pages/FavoritesPage';
import SharedPage from '@/pages/SharedPage';
import PublicNotesPage from '@/pages/PublicNotesPage';
import PublicNotePage from '@/pages/PublicNotePage';
import ProfilePage from '@/pages/ProfilePage';

// Composant pour les routes protégées
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return <LoadingSpinner />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

// Composant pour les routes publiques (redirection si connecté)
const PublicRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return <LoadingSpinner />;
  }

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
};

const App: React.FC = () => {
  const { isLoading } = useAuth();

  if (isLoading) {
    return <LoadingSpinner />;
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <Navbar />
      
      <Box component="main" sx={{ flexGrow: 1, p: 3 }}>
        <Routes>
          {/* Routes publiques */}
          <Route 
            path="/login" 
            element={
              <PublicRoute>
                <LoginPage />
              </PublicRoute>
            } 
          />
          <Route 
            path="/register" 
            element={
              <PublicRoute>
                <RegisterPage />
              </PublicRoute>
            } 
          />
          <Route 
            path="/public" 
            element={<PublicNotesPage />} 
          />
          <Route 
            path="/p/:token" 
            element={<PublicNotePage />} 
          />

          {/* Routes protégées */}
          <Route 
            path="/dashboard" 
            element={
              <ProtectedRoute>
                <DashboardPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/notes" 
            element={
              <ProtectedRoute>
                <NotesPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/notes/create" 
            element={
              <ProtectedRoute>
                <NoteEditorPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/notes/:id/edit" 
            element={
              <ProtectedRoute>
                <NoteEditorPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/notes/:id" 
            element={
              <ProtectedRoute>
                <NoteViewPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/favorites" 
            element={
              <ProtectedRoute>
                <FavoritesPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/shared" 
            element={
              <ProtectedRoute>
                <SharedPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/profile" 
            element={
              <ProtectedRoute>
                <ProfilePage />
              </ProtectedRoute>
            } 
          />

          {/* Route par défaut */}
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          
          {/* Route 404 */}
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </Box>
    </Box>
  );
};

export default App;


