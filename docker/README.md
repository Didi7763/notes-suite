# Configuration Docker - Notes Suite

Ce dossier contient les configurations Docker pour l'application Notes Suite.

## Structure

```
docker/
├── postgres/
│   └── init-db.sql          # Script d'initialisation de la base de données
├── nginx/
│   └── nginx.conf           # Configuration Nginx (reverse proxy optionnel)
└── README.md               # Ce fichier
```

## Base de données PostgreSQL

### Script d'initialisation (`postgres/init-db.sql`)

Le script d'initialisation crée :
- Les tables principales : `users`, `notes`, `note_tags`
- Les index pour optimiser les performances
- Les triggers pour la mise à jour automatique des timestamps
- Des données de test (utilisateurs et notes d'exemple)

### Utilisateurs de test

- **Admin** : `admin` / `password123`
- **Utilisateur** : `testuser` / `password123`

## Nginx (optionnel)

La configuration Nginx peut être utilisée pour :
- Reverse proxy vers les services backend et frontend
- Gestion du CORS
- Load balancing (si nécessaire)

## Utilisation

Les configurations Docker sont utilisées via le `docker-compose.yml` à la racine du projet.

### Commandes utiles

```bash
# Démarrer tous les services
docker compose up -d

# Voir les logs
docker compose logs -f

# Arrêter tous les services
docker compose down

# Redémarrer un service spécifique
docker compose restart api

# Accéder à la base de données
docker compose exec postgres psql -U notes_user -d notes_db
```

## Variables d'environnement

Les variables d'environnement sont définies dans le `docker-compose.yml` :

- `POSTGRES_DB` : Nom de la base de données
- `POSTGRES_USER` : Utilisateur PostgreSQL
- `POSTGRES_PASSWORD` : Mot de passe PostgreSQL
- `JWT_SECRET` : Clé secrète pour JWT
- `JWT_EXPIRATION` : Durée de validité du token JWT
- `REACT_APP_API_URL` : URL de l'API pour le frontend
