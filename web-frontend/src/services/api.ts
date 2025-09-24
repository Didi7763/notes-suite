import axios, { AxiosInstance, AxiosResponse, AxiosError } from 'axios';
import { 
  AuthResponse, 
  LoginRequest, 
  RegisterRequest, 
  Note, 
  NoteCreateRequest, 
  NoteUpdateRequest,
  NoteFilters,
  ApiResponse,
  Share,
  ShareCreateRequest,
  PublicLink,
  PublicLinkCreateRequest,
  User
} from '@/types';

class ApiService {
  private api: AxiosInstance;
  private refreshToken: string | null = null;

  constructor() {
    this.api = axios.create({
      baseURL: '/api/v1',
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Intercepteur pour ajouter le token d'authentification
    this.api.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('accessToken');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Intercepteur pour gérer les erreurs et le refresh token
    this.api.interceptors.response.use(
      (response) => response,
      async (error: AxiosError) => {
        const originalRequest = error.config;
        
        if (error.response?.status === 401 && originalRequest && !originalRequest._retry) {
          originalRequest._retry = true;
          
          try {
            const newToken = await this.refreshAccessToken();
            if (newToken) {
              originalRequest.headers.Authorization = `Bearer ${newToken}`;
              return this.api(originalRequest);
            }
          } catch (refreshError) {
            this.logout();
            window.location.href = '/login';
          }
        }
        
        return Promise.reject(error);
      }
    );
  }

  // Méthodes d'authentification
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response: AxiosResponse<AuthResponse> = await this.api.post('/auth/login', credentials);
    this.setTokens(response.data);
    return response.data;
  }

  async register(userData: RegisterRequest): Promise<AuthResponse> {
    const response: AxiosResponse<AuthResponse> = await this.api.post('/auth/register', userData);
    this.setTokens(response.data);
    return response.data;
  }

  async logout(): Promise<void> {
    const refreshToken = localStorage.getItem('refreshToken');
    if (refreshToken) {
      try {
        await this.api.post('/auth/logout', { refreshToken });
      } catch (error) {
        console.error('Erreur lors de la déconnexion:', error);
      }
    }
    this.clearTokens();
  }

  async getCurrentUser(): Promise<User> {
    const response: AxiosResponse<User> = await this.api.get('/auth/me');
    return response.data;
  }

  async checkEmailAvailability(email: string): Promise<{ available: boolean }> {
    const response = await this.api.get(`/auth/check-email?email=${encodeURIComponent(email)}`);
    return response.data;
  }

  // Méthodes pour les notes
  async getNotes(filters: NoteFilters = {}): Promise<ApiResponse<Note>> {
    const params = new URLSearchParams();
    
    if (filters.query) params.append('query', filters.query);
    if (filters.tag) params.append('tag', filters.tag);
    if (filters.visibility) params.append('visibility', filters.visibility);
    if (filters.page !== undefined) params.append('page', filters.page.toString());
    if (filters.size !== undefined) params.append('size', filters.size.toString());
    if (filters.sortBy) params.append('sortBy', filters.sortBy);
    if (filters.sortDir) params.append('sortDir', filters.sortDir);

    const response: AxiosResponse<ApiResponse<Note>> = await this.api.get(`/notes?${params.toString()}`);
    return response.data;
  }

  async getNote(id: number): Promise<Note> {
    const response: AxiosResponse<Note> = await this.api.get(`/notes/${id}`);
    return response.data;
  }

  async createNote(noteData: NoteCreateRequest): Promise<Note> {
    const response: AxiosResponse<Note> = await this.api.post('/notes', noteData);
    return response.data;
  }

  async updateNote(id: number, noteData: NoteUpdateRequest): Promise<Note> {
    const response: AxiosResponse<Note> = await this.api.put(`/notes/${id}`, noteData);
    return response.data;
  }

  async deleteNote(id: number): Promise<void> {
    await this.api.delete(`/notes/${id}`);
  }

  async toggleFavorite(id: number): Promise<Note> {
    const response: AxiosResponse<Note> = await this.api.post(`/notes/${id}/favorite`);
    return response.data;
  }

  async getFavoriteNotes(page: number = 0, size: number = 20): Promise<ApiResponse<Note>> {
    const response: AxiosResponse<ApiResponse<Note>> = await this.api.get(`/notes/favorites?page=${page}&size=${size}`);
    return response.data;
  }

  async getSharedNotes(page: number = 0, size: number = 20): Promise<ApiResponse<Note>> {
    const response: AxiosResponse<ApiResponse<Note>> = await this.api.get(`/notes/shared?page=${page}&size=${size}`);
    return response.data;
  }

  // Méthodes pour le partage
  async shareNoteWithUser(noteId: number, shareData: ShareCreateRequest): Promise<Share> {
    const response: AxiosResponse<Share> = await this.api.post(`/notes/${noteId}/share/user`, shareData);
    return response.data;
  }

  async getNoteShares(noteId: number): Promise<Share[]> {
    const response: AxiosResponse<Share[]> = await this.api.get(`/notes/${noteId}/shares`);
    return response.data;
  }

  async getReceivedShares(page: number = 0, size: number = 20): Promise<ApiResponse<Share>> {
    const response: AxiosResponse<ApiResponse<Share>> = await this.api.get(`/shares/received?page=${page}&size=${size}`);
    return response.data;
  }

  async deleteShare(shareId: number): Promise<void> {
    await this.api.delete(`/shares/${shareId}`);
  }

  async revokeShare(shareId: number): Promise<void> {
    await this.api.post(`/shares/${shareId}/revoke`);
  }

  async revokeAllNoteShares(noteId: number): Promise<void> {
    await this.api.post(`/notes/${noteId}/shares/revoke-all`);
  }

  // Méthodes pour les liens publics
  async createPublicLink(noteId: number, linkData: PublicLinkCreateRequest): Promise<PublicLink> {
    const response: AxiosResponse<PublicLink> = await this.api.post(`/notes/${noteId}/share/public`, linkData);
    return response.data;
  }

  async getNotePublicLinks(noteId: number): Promise<PublicLink[]> {
    const response: AxiosResponse<PublicLink[]> = await this.api.get(`/notes/${noteId}/public-links`);
    return response.data;
  }

  async deletePublicLink(linkId: number): Promise<void> {
    await this.api.delete(`/public-links/${linkId}`);
  }

  async deletePublicLinkByToken(token: string): Promise<void> {
    await this.api.delete(`/public-links/token/${token}`);
  }

  async deactivatePublicLink(linkId: number): Promise<PublicLink> {
    const response: AxiosResponse<PublicLink> = await this.api.post(`/public-links/${linkId}/deactivate`);
    return response.data;
  }

  // Méthodes pour l'accès public
  async accessPublicNote(token: string, password?: string): Promise<{ note: Note; publicLink: PublicLink }> {
    const response = await this.api.get(`/p/${token}${password ? `?password=${encodeURIComponent(password)}` : ''}`);
    return response.data;
  }

  async getPublicLinkInfo(token: string): Promise<PublicLink> {
    const response: AxiosResponse<PublicLink> = await this.api.get(`/p/${token}/info`);
    return response.data;
  }

  async verifyPublicLinkPassword(token: string, password: string): Promise<{ valid: boolean }> {
    const response = await this.api.post(`/p/${token}/verify-password`, { password });
    return response.data;
  }

  // Méthodes utilitaires
  private async refreshAccessToken(): Promise<string | null> {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) return null;

    try {
      const response: AxiosResponse<AuthResponse> = await this.api.post('/auth/refresh', { refreshToken });
      this.setTokens(response.data);
      return response.data.accessToken;
    } catch (error) {
      this.clearTokens();
      return null;
    }
  }

  private setTokens(authData: AuthResponse): void {
    localStorage.setItem('accessToken', authData.accessToken);
    localStorage.setItem('refreshToken', authData.refreshToken);
    this.refreshToken = authData.refreshToken;
  }

  private clearTokens(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    this.refreshToken = null;
  }

  // Méthode pour vérifier si l'utilisateur est authentifié
  isAuthenticated(): boolean {
    return !!localStorage.getItem('accessToken');
  }

  // Méthode pour obtenir le token d'accès
  getAccessToken(): string | null {
    return localStorage.getItem('accessToken');
  }
}

// Instance singleton
export const apiService = new ApiService();
export default apiService;


