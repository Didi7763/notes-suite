# Guide de Développement - Notes Suite

## 🚀 Démarrage Rapide

### Prérequis

- **Docker** et **Docker Compose**
- **Node.js** 18+ et **npm**
- **JDK** 17+
- **Android Studio** (pour le mobile)
- **Xcode** (pour iOS, macOS uniquement)

### Installation et Démarrage

1. **Cloner le repository**
```bash
git clone <votre-repo>
cd notes-suite
```

2. **Démarrer les services backend + web**
```bash
# Démarrer PostgreSQL, API et Web
docker compose up -d

# Vérifier que les services sont démarrés
docker compose ps
```

3. **Accéder aux applications**
- **Web** : http://localhost:8081
- **API** : http://localhost:8080
- **Base de données** : localhost:5432

4. **Démarrer l'application mobile**
```bash
cd mobile-app
npm install

# Android
npx react-native run-android

# iOS (macOS uniquement)
npx react-native run-ios
```

## 🏗️ Architecture du Projet

```
notes-suite/
├── backend-spring/          # API Spring Boot 3
├── web-frontend/           # Application React TypeScript
├── mobile-app/             # Application React Native
├── docker/                 # Configurations Docker
├── docs/                   # Documentation
├── docker-compose.yml      # Orchestration des services
└── README.md              # Documentation principale
```

## 🔧 Développement

### Backend (Spring Boot)

```bash
cd backend-spring

# Développement local (avec base de données Docker)
./mvnw spring-boot:run

# Tests
./mvnw test

# Build
./mvnw clean package
```

**Endpoints principaux :**
- `POST /api/v1/auth/signin` - Connexion
- `POST /api/v1/auth/signup` - Inscription
- `GET /api/v1/notes` - Liste des notes
- `POST /api/v1/notes` - Créer une note
- `PUT /api/v1/notes/{id}` - Modifier une note
- `DELETE /api/v1/notes/{id}` - Supprimer une note

### Frontend Web (React)

```bash
cd web-frontend

# Installation des dépendances
npm install

# Développement
npm start

# Build de production
npm run build

# Tests
npm test
```

**Technologies :**
- React 18 + TypeScript
- Material-UI (MUI)
- React Router v6
- React Query
- React Hook Form + Yup

### Mobile (React Native)

```bash
cd mobile-app

# Installation des dépendances
npm install

# iOS (macOS uniquement)
cd ios && pod install && cd ..
npx react-native run-ios

# Android
npx react-native run-android

# Tests
npm test
```

**Fonctionnalités :**
- Mode offline avec synchronisation
- Navigation par onglets
- Authentification JWT
- Stockage local avec AsyncStorage

## 🐳 Docker

### Services disponibles

- **postgres** : Base de données PostgreSQL 15
- **api** : API Spring Boot
- **web** : Frontend React (Nginx)

### Commandes utiles

```bash
# Démarrer tous les services
docker compose up -d

# Voir les logs
docker compose logs -f [service]

# Redémarrer un service
docker compose restart [service]

# Arrêter tous les services
docker compose down

# Rebuild et redémarrer
docker compose up --build -d
```

## 🗄️ Base de données

### Connexion

```bash
# Via Docker
docker compose exec postgres psql -U notes_user -d notes_db

# Via client externe
psql -h localhost -p 5432 -U notes_user -d notes_db
```

### Utilisateurs de test

- **Admin** : `admin` / `password123`
- **Utilisateur** : `testuser` / `password123`

### Structure des tables

```sql
-- Utilisateurs
users (id, username, email, password, first_name, last_name, role, created_at, updated_at)

-- Notes
notes (id, title, content, status, is_public, created_at, updated_at, user_id)

-- Tags de notes
note_tags (id, name, color, note_id)
```

## 🔐 Authentification

### JWT Configuration

- **Secret** : Configuré via `JWT_SECRET`
- **Expiration** : 24h par défaut
- **Algorithme** : HS256

### Headers requis

```http
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

## 📱 Mobile - Mode Offline

### Fonctionnalités offline

1. **Détection de connectivité** : NetInfo
2. **Stockage local** : AsyncStorage
3. **Synchronisation** : Automatique à la reconnexion
4. **Indicateurs visuels** : Statut de synchronisation

### Gestion des données offline

```typescript
// Sauvegarder une note offline
await offlineStorage.saveOfflineNote(note);

// Marquer pour synchronisation
await offlineStorage.markForSync(noteId);

// Synchroniser à la reconnexion
await syncOfflineNotes();
```

## 🧪 Tests

### Backend

```bash
cd backend-spring
./mvnw test
```

### Frontend Web

```bash
cd web-frontend
npm test
```

### Mobile

```bash
cd mobile-app
npm test
```

## 🚀 Déploiement

### Production

1. **Backend** : Build Docker + déploiement
2. **Frontend** : Build statique + Nginx
3. **Mobile** : Build APK/IPA
4. **Database** : PostgreSQL avec persistence

### Variables d'environnement

```bash
# Backend
JWT_SECRET=your-secret-key
DATABASE_URL=jdbc:postgresql://postgres:5432/notes_db
DATABASE_USERNAME=notes_user
DATABASE_PASSWORD=notes_password

# Frontend
REACT_APP_API_URL=http://localhost:8080/api/v1
```

## 🐛 Débogage

### Logs

```bash
# Tous les services
docker compose logs -f

# Service spécifique
docker compose logs -f api
docker compose logs -f postgres
```

### Base de données

```bash
# Accéder à la base
docker compose exec postgres psql -U notes_user -d notes_db

# Voir les tables
\dt

# Voir les données
SELECT * FROM users;
SELECT * FROM notes;
```

### Mobile

```bash
# Logs Android
npx react-native log-android

# Logs iOS
npx react-native log-ios
```

## 📚 Ressources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://reactjs.org/docs)
- [React Native Documentation](https://reactnative.dev/docs/getting-started)
- [Material-UI Documentation](https://mui.com/)
- [React Native Paper Documentation](https://reactnativepaper.com/)

## 🤝 Contribution

1. Fork le projet
2. Créer une branche feature (`git checkout -b feature/AmazingFeature`)
3. Commit les changements (`git commit -m 'Add some AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request
