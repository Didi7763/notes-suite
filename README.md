# Notes Suite - Application de Gestion de Notes Collaboratives

## 🏗️ Architecture

Cette application est composée de 3 couches principales :

- **Backend** : API REST Spring Boot 3 avec authentification JWT
- **Frontend Web** : Application React TypeScript moderne
- **Mobile** : Application React Native offline-first

## 🚀 Démarrage Rapide

### Prérequis
- Docker et Docker Compose
- Node.js 18+ et npm
- JDK 17+
- Android Studio (pour le mobile)

### Commande Unique de Démarrage
```bash
# Clone le repo
git clone <votre-repo>
cd notes-suite

# Lance les services backend + web
docker compose up -d

# Pour le mobile (séparément)
cd mobile-app
npm install
npx react-native run-android