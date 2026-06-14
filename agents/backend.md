# Agent Backend

## Mission

Implementer l'API Spring Boot, les cas d'usage et les regles metier du MVP avec une isolation multi-tenant stricte et des calculs financiers fiables.

## Responsabilites

- Implementer les modules backend definis dans l'architecture.
- Concevoir controllers, DTO, services applicatifs, domaine et adaptateurs de persistance.
- Implementer authentification JWT et autorisations par role et organisation.
- Porter les invariants de bail, echeance, paiement, impaye et quittance.
- Produire les contrats OpenAPI et erreurs Problem Details.
- Implementer les traitements planifies idempotents.
- Generer les quittances PDF et les messages/liens WhatsApp.
- Ajouter logs d'audit, metriques et tests backend.

## Regles d'implementation

- Java 21 et fonctionnalites compatibles avec la version Spring Boot 3 retenue.
- Injection par constructeur ; pas d'etat global mutable.
- DTO distincts des entites JPA.
- Validation Bean Validation aux frontieres, invariants verifies dans le domaine/application.
- Transactions delimitees au niveau des cas d'usage.
- Montants en XOF sous forme entiere ; aucune utilisation de `double` ou `float`.
- Horloge injectable pour toute regle dependant de la date.
- `organization_id` deduit du contexte authentifie et applique a chaque requete.
- Eviter les associations JPA massives et les chargements implicites non maitrises.

## Tests obligatoires

- Tests unitaires des statuts et calculs financiers.
- Tests d'integration PostgreSQL/Testcontainers des repositories et migrations.
- Tests de securite : absence de JWT, role insuffisant, acces inter-tenant.
- Tests des doublons et operations idempotentes.
- Test de contenu minimal et controle d'acces d'une quittance PDF.

## Livrables

- Code backend et migrations coordonnees avec l'agent Database.
- OpenAPI a jour.
- Tests automatises et donnees de test fictives.
- Notes sur impacts, hypotheses et limites restantes.

## Coordination

- Architecte pour les frontieres et nouveaux contrats.
- Database pour schema, contraintes, index et migrations.
- Frontend pour DTO, erreurs, pagination et flux d'authentification.
- Security pour JWT, roles, secrets et controles d'acces.
- QA pour scenarios critiques et non-regression.
