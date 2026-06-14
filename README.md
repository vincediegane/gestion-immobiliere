# Real Estate SaaS MVP

Plateforme SaaS de gestion immobiliere pour le Senegal.

Le Sprint 1 fournit uniquement le socle technique :

- backend Spring Boot 3 / Java 21 / Maven ;
- frontend React / TypeScript / Vite / Material UI ;
- PostgreSQL avec Docker Compose ;
- healthcheck backend `GET /api/health` ;
- page d'accueil frontend `Real Estate SaaS MVP`.

Aucun module metier (proprietaire, bien, locataire, bail ou paiement) n'est implemente dans ce sprint.

## Prerequis

- Docker Desktop avec Docker Compose ;
- Java 21 pour lancer le backend hors conteneur ;
- Node.js 22.13 ou plus recent pour le frontend.

Maven n'a pas besoin d'etre installe globalement : le projet fournit Maven Wrapper.

## 1. Demarrer PostgreSQL

Depuis la racine du depot :

```powershell
docker compose up -d postgres
docker compose ps
```

Configuration locale par defaut :

| Parametre | Valeur |
|---|---|
| Hote | `localhost` |
| Port | `5432` |
| Base | `real_estate` |
| Utilisateur | `real_estate` |
| Mot de passe | `real_estate_dev` |

Ces valeurs sont reservees au developpement local. Elles peuvent etre surchargees avec `DB_NAME`, `DB_USER`, `DB_PASSWORD` et `DB_PORT`.

Pour arreter la base :

```powershell
docker compose down
```

Pour supprimer egalement les donnees locales :

```powershell
docker compose down -v
```

## 2. Demarrer le backend

PostgreSQL doit etre disponible avant le demarrage.

Sous Windows PowerShell :

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Sous Linux ou macOS :

```bash
cd backend
./mvnw spring-boot:run
```

Le backend est disponible sur `http://localhost:8080`.

Verifier le healthcheck :

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

Reponse attendue :

```json
{
  "status": "UP",
  "service": "real-estate-saas-backend"
}
```

Variables backend disponibles : `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` et `SERVER_PORT`.

## 3. Demarrer le frontend

Dans un second terminal :

```powershell
cd frontend
npm ci
npm run dev
```

Le frontend est disponible sur `http://localhost:5173` et affiche `Real Estate SaaS MVP`.

## Verification

Backend :

```powershell
cd backend
.\mvnw.cmd test
```

Frontend :

```powershell
cd frontend
npm run lint
npm run build
```

Configuration Docker Compose :

```powershell
docker compose config
```

## Structure

```text
backend/              Application Spring Boot et migrations Flyway
frontend/             Application React TypeScript et Material UI
agents/               Roles des agents du projet
docs/                 Backlog MVP et architecture technique
docker-compose.yml    PostgreSQL local
AGENTS.md              Regles globales du projet
README.md              Instructions de demarrage
```

## Documentation

- `AGENTS.md` : regles globales et Definition of Done.
- `docs/BACKLOG.md` : epics, user stories et criteres d'acceptation.
- `docs/ARCHITECTURE.md` : architecture cible du MVP.
