# Gestion Immobiliere SaaS - Etat du Projet

## Position actuelle

Le projet est entre **MVP fonctionnel** et **stabilisation avant demo client**.

Le backend, le frontend, PostgreSQL, l'authentification JWT, les migrations Flyway et les principales fonctionnalites metier sont presents.

## Fonctionnalites implementees

### Backend

- Authentification JWT avec roles `ADMIN` et `GESTIONNAIRE`.
- Isolation applicative par `organization_id`.
- Modules Owner, Property, Unit, Tenant, Lease, Billing/Payment, Receipt, Reminder et Dashboard.
- Generation idempotente des echeances mensuelles manquantes.
- Paiements avec verrou pessimiste de l'echeance et cle d'idempotence.
- Un seul bail actif par unite via validation applicative et contrainte SQL.
- Quittance PDF pour les echeances soldees.
- Preparation de lien WhatsApp sans envoi automatique.
- Documentation OpenAPI/Swagger.
- Tests unitaires backend.

### Frontend

- Authentification.
- Dashboard.
- CRUD principaux.
- Selection des relations par listes/autocomplete.
- Navigation responsive.
- Interface modernisee avec Tailwind CSS et Material UI.

### Infrastructure

- Docker Compose.
- PostgreSQL non expose sur l'hote par defaut.
- Backend et frontend buildables en conteneurs.
- Migrations Flyway `V1` a `V7`, dont un seed local de demonstration.
- Secrets obligatoires via variables d'environnement.

## Stabilisation appliquee

- Paiements concurrents proteges par verrou transactionnel pessimiste.
- Idempotence de paiement renforcee : une meme cle avec un contenu different est refusee.
- Scheduler multi-organisation sans dependance au contexte HTTP.
- Traitement scheduler isole par organisation.
- Rattrapage des echeances historiques pour baux actifs et termines.
- Terminaison de bail refusee si date future ou anterieure au debut.
- Dashboard : les impayes comptent uniquement les echeances echues non soldees.
- React Router mis a jour pour supprimer les vulnerabilites npm connues.

## Dettes restantes avant pilote client

- Ajouter des tests d'integration PostgreSQL/Testcontainers pour la concurrence paiement, Flyway et l'isolation multi-tenant.
- Revalider le statut utilisateur/organisation pendant la vie du JWT ou raccourcir fortement la duree du jeton.
- Ajouter rate limiting sur `/api/auth/login`.
- Versionner l'API en `/api/v1` ou documenter explicitement le maintien temporaire de `/api`.
- Ajouter snapshot des donnees de quittance pour rendre les PDF historiques immuables.
- Ajouter une politique CSP et des en-tetes de securite Nginx.
- Decouper le bundle frontend si la taille devient un probleme.

## Parcours demo a revalider

```txt
Owner
 -> Property
 -> Unit
 -> Tenant
 -> Lease
 -> Payment
 -> Receipt PDF
 -> Dashboard
```
