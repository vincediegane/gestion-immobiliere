# Real Estate SaaS MVP

Plateforme SaaS de gestion immobiliere pour le Senegal.

L'application comprend actuellement :

- backend Spring Boot 3 / Java 21 / Maven ;
- frontend React / TypeScript / Vite / Material UI ;
- modules de gestion des proprietaires et des biens immobiliers ;
- documentation OpenAPI et interface Swagger UI ;
- PostgreSQL, backend et frontend entierement dockerises ;
- healthcheck backend `GET /api/health` ;
- page d'accueil frontend `Real Estate SaaS MVP`.

Les modules Unit, Tenant, Lease et Payment ne sont pas encore implementes.

## Lancement avec Docker

Le seul prerequis est Docker avec Docker Compose. Java, Maven, Node.js et Nginx sont fournis par les images.

Depuis la racine du depot :

```bash
docker compose up --build -d
docker compose ps
```

Services disponibles :

- frontend : `http://localhost:5173` ;
- backend direct : `http://localhost:8080` ;
- API via le frontend/Nginx : `http://localhost:5173/api/owners` ;
- Swagger UI : `http://localhost:8080/swagger-ui/index.html` ;
- specification OpenAPI : `http://localhost:8080/v3/api-docs` ;
- PostgreSQL : `localhost:5432`.

Les corps JSON envoyes a l'API doivent etre encodes en UTF-8. Utiliser de preference l'en-tete `Content-Type: application/json; charset=UTF-8`, notamment pour les noms et adresses avec accents.

Verifier les services :

```bash
curl http://localhost:8080/api/health
curl http://localhost:5173/api/health
```

Endpoints Property disponibles :

- `POST /api/properties` ;
- `GET /api/properties` ;
- `GET /api/properties/{id}` ;
- `PUT /api/properties/{id}` ;
- `DELETE /api/properties/{id}` ;
- `GET /api/owners/{ownerId}/properties`.

Afficher les logs :

```bash
docker compose logs -f
docker compose logs -f backend
```

Arreter l'application :

```bash
docker compose down
```

Arreter l'application et supprimer les donnees PostgreSQL locales :

```bash
docker compose down -v
```

## Tests sans Maven local

La construction de l'image backend execute automatiquement les tests Maven :

```bash
docker compose build backend
```

Pour executer explicitement l'equivalent de `mvn clean test` dans un conteneur :

```bash
docker compose run --rm backend-tests
```

Le service `backend-tests` utilise une image Maven dediee et un volume `maven_cache`. Il ne fait pas partie des services permanents lances par `docker compose up`.

L'image d'execution `backend` contient seulement Java et le JAR de l'application. La commande suivante ne peut donc pas fonctionner et retournerait `mvn: executable file not found` :

```bash
docker compose exec backend mvn clean test
```

Le build frontend execute `npm run lint` puis `npm run build` :

```bash
docker compose build frontend
```

## Configuration

Configuration locale par defaut :

| Parametre | Valeur |
|---|---|
| Hote | `localhost` |
| Port | `5432` |
| Base | `real_estate` |
| Utilisateur | `real_estate` |
| Mot de passe | `real_estate_dev` |

Reponse attendue du healthcheck :

```json
{
  "status": "UP",
  "service": "real-estate-saas-backend"
}
```

Variables Docker Compose disponibles :

- `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `DB_PORT` ;
- `BACKEND_PORT` et `FRONTEND_PORT` ;
- `DEFAULT_ORGANIZATION_ID`.

`DEFAULT_ORGANIZATION_ID` fournit le tenant technique temporaire des modules Owner et Property tant que l'authentification et le module Organization ne sont pas encore implementes.

Verifier la configuration Compose sans demarrer les services :

```bash
docker compose config
```

## Structure

```text
backend/              Spring Boot, migration Flyway et Dockerfile Java 21
frontend/             React, configuration Nginx et Dockerfile Node/Nginx
agents/               Roles des agents du projet
docs/                 Backlog MVP et architecture technique
docker-compose.yml    PostgreSQL, backend et frontend
AGENTS.md              Regles globales du projet
README.md              Instructions de demarrage
```

## Documentation

- `AGENTS.md` : regles globales et Definition of Done.
- `docs/BACKLOG.md` : epics, user stories et criteres d'acceptation.
- `docs/ARCHITECTURE.md` : architecture cible du MVP.
