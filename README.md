# Real Estate SaaS MVP

Plateforme SaaS de gestion immobiliere pour le Senegal.

L'application comprend :

- backend Spring Boot 3 / Java 21 / Maven ;
- frontend React / TypeScript / Vite / Material UI / Tailwind CSS ;
- authentification JWT avec roles `ADMIN` et `GESTIONNAIRE` ;
- gestion des proprietaires, biens, unites et locataires ;
- gestion des baux et generation idempotente de toutes leurs echeances mensuelles manquantes, y compris le rattrapage des baux termines ;
- paiements partiels ou complets, verrouilles contre les depassements concurrents, avec cle d'idempotence stricte ;
- preparation de relances WhatsApp sans envoi automatique ;
- generation de quittances PDF pour les echeances soldees ;
- tableau de bord avec compteurs et montants XOF ;
- documentation OpenAPI et interface Swagger UI ;
- PostgreSQL, backend et frontend entierement dockerises ;
- healthcheck backend `GET /api/health` ;
- page d'accueil frontend `Real Estate SaaS MVP` ;
- donnees de demonstration chargees par Flyway en environnement local.

Le MVP fonctionne comme un monolithe modulaire avec une organisation locale initiale.

## Lancement avec Docker

Le seul prerequis est Docker avec Docker Compose. Java, Maven, Node.js et Nginx sont fournis par les images.

Depuis la racine du depot :

```bash
cp .env.example .env
# Remplacer toutes les valeurs sensibles dans .env
docker compose up --build -d
docker compose ps
```

Services disponibles :

- frontend : `http://localhost:5173` ;
- backend direct : `http://localhost:8080` ;
- API via le frontend/Nginx : `http://localhost:5173/api/owners` ;
- Swagger UI : `http://localhost:8080/swagger-ui/index.html` ;
- specification OpenAPI : `http://localhost:8080/v3/api-docs` ;
- PostgreSQL n'est pas expose sur l'hote par defaut.

Les identifiants administrateur et tous les secrets sont obligatoires dans `.env`. Le depot ne fournit aucun mot de passe utilisable par defaut.

Comptes de demonstration locaux apres application des migrations :

| Role | Email | Mot de passe |
|---|---|---|
| ADMIN | `admin@gestion.local` | `Passer123` |
| GESTIONNAIRE | `gestionnaire@gestion.local` | `Passer123` |

La migration `V7__seed_demo_data.sql` cree aussi un jeu de donnees fictif : 3 proprietaires, 3 biens, 4 unites, 3 locataires, 3 baux, 8 echeances, 6 paiements, 2 quittances et quelques relances preparees.

Pour exposer temporairement PostgreSQL sur `localhost:5432` en developpement :

```bash
docker compose --profile dev up -d postgres-dev
```

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

Principales familles d'endpoints :

- `/api/auth/login` ;
- `/api/owners`, `/api/properties`, `/api/units`, `/api/tenants` ;
- `/api/leases` et `/api/leases/{id}/terminate` ;
- `/api/rent-charges` et `/api/rent-charges/{id}/payments` ;
- `/api/rent-charges/{id}/reminder-preview` ;
- `/api/rent-charges/{id}/receipt` et `/api/receipts/{id}/pdf` ;
- `/api/dashboard/summary`.

Toutes les routes metier exigent `Authorization: Bearer <token>`. Les suppressions exigent le role `ADMIN`.

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

Configuration PostgreSQL interne :

| Parametre | Valeur |
|---|---|
| Hote | `localhost` |
| Port | `5432` |
| Base | `real_estate` |
| Utilisateur | `real_estate` |
| Mot de passe | valeur obligatoire de `DB_PASSWORD` |

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
- `JWT_SECRET`, `JWT_VALIDITY_MINUTES` ;
- `BOOTSTRAP_ADMIN_EMAIL`, `BOOTSTRAP_ADMIN_PASSWORD`.

`DB_PASSWORD`, `JWT_SECRET`, `BOOTSTRAP_ADMIN_EMAIL` et `BOOTSTRAP_ADMIN_PASSWORD` sont obligatoires. Utiliser `.env.example` comme modele, sans committer le fichier `.env` reel.

Les paiements exigent l'en-tete `Idempotency-Key` avec une valeur unique par tentative logique. Le rejeu de la meme cle retourne le paiement deja enregistre sans debiter une seconde fois.

`DEFAULT_ORGANIZATION_ID` fournit l'organisation initiale locale. Pour une requete authentifiee, l'organisation effective provient du claim JWT `organization_id`.

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
- `docs/MVP_STATUS.md` : perimetre livre, validations et limites connues.
