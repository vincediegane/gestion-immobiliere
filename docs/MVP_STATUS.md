# Etat du MVP

## Perimetre livre

- Authentification JWT et isolation par `organization_id`.
- CRUD Owner, Property, Unit et Tenant.
- Creation, consultation et terminaison d'un bail.
- Generation de la premiere echeance lors de la creation d'un bail, puis generation mensuelle idempotente pour les baux actifs.
- Paiements partiels ou complets avec interdiction du depassement.
- Statuts d'echeance `UPCOMING`, `DUE`, `PARTIAL`, `PAID`, `OVERDUE`.
- Preparation d'un message et d'un lien `wa.me` sans envoi automatique.
- Quittance PDF disponible uniquement pour une echeance soldee.
- Tableau de bord des referentiels, baux, loyers attendus, encaisses et impayes.
- Interface React responsive pour les parcours principaux.
- Docker Compose pour PostgreSQL, backend et frontend.

## Decisions MVP

- Monolithe modulaire et schema PostgreSQL partage.
- Une organisation et un administrateur sont initialises localement au demarrage.
- Le token d'acces est conserve dans `sessionStorage` et expire apres huit heures par defaut.
- Les suppressions sont reservees au role `ADMIN`; les referentiels utilisent un archivage logique.
- La quittance est regeneree depuis les donnees courantes de l'echeance et du bail.
- La generation mensuelle s'execute chaque jour et lors de la consultation des loyers afin de rattraper une execution manquee.

## Validation

- Compilation backend Java 21 dans Docker.
- Tests JUnit/Mockito executes pendant le build backend.
- Lint TypeScript et build Vite executes pendant le build frontend.
- Migrations Flyway V1 a V5 appliquees sur PostgreSQL.
- Parcours reel valide jusqu'au paiement complet et a la generation PDF.

## Limites avant production

- Changer tous les secrets et identifiants bootstrap.
- Ajouter rotation/refresh/revocation des jetons.
- Ajouter une gestion d'utilisateurs complete et l'initialisation multi-organisation administree.
- Ajouter des tests d'integration Testcontainers et des tests frontend React Testing Library.
- Ajouter audit fonctionnel, correlation ID, sauvegardes et supervision de production.
- Ajouter un verrou distribue a la generation mensuelle si plusieurs instances backend sont deployees.
