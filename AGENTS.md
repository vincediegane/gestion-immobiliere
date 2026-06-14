# AGENTS.md - Regles globales du projet

## 1. Mission du produit

Construire une plateforme SaaS de gestion immobiliere adaptee au Senegal. Le MVP doit permettre a une organisation de gerer ses proprietaires, biens, unites, locataires, baux et loyers, de detecter les impayes, de preparer des relances WhatsApp et de generer des quittances PDF.

Le produit doit rester simple pour un gestionnaire immobilier, fiable sur les calculs financiers et strict sur l'isolation des donnees entre organisations.

## 2. Perimetre du MVP

Inclus :

- authentification et autorisation par JWT ;
- gestion des utilisateurs d'une organisation ;
- gestion des proprietaires ;
- gestion des biens immobiliers et de leurs unites ;
- gestion des locataires ;
- gestion des contrats de bail ;
- generation et suivi des echeances de loyer ;
- enregistrement des paiements ;
- detection des retards et impayes ;
- preparation d'un message et d'un lien WhatsApp ;
- generation et telechargement des quittances PDF ;
- tableau de bord operationnel minimal ;
- execution locale avec Docker Compose et migrations Flyway.

Exclus du MVP, sauf decision explicite du Product Owner :

- comptabilite generale et rapprochement bancaire ;
- paiement en ligne ou mobile money ;
- envoi automatique de messages WhatsApp ;
- signature electronique des baux ;
- gestion avancee des travaux, fournisseurs et etats des lieux ;
- portail proprietaire ou locataire autonome ;
- application mobile native ;
- tarification et facturation de l'abonnement SaaS.

## 3. Stack imposee

- Backend : Java 21, Spring Boot 3, Maven ou Gradle selon le choix initialise dans le depot.
- Frontend : React, TypeScript, Material UI.
- Base de donnees : PostgreSQL.
- Migrations : Flyway, versionnees et immuables apres integration.
- Authentification : JWT avec mots de passe hashes.
- Environnement local : Docker Compose.
- API : REST JSON documentee avec OpenAPI.
- PDF : generation cote backend.

Ne pas remplacer une technologie imposee sans decision d'architecture documentee et validation du Product Owner.

## 4. Principes d'architecture

- Demarrer par un monolithe modulaire, pas par des microservices.
- Organiser le backend par domaine fonctionnel, avec des dependances explicites entre modules.
- Separer les couches API, application, domaine et infrastructure lorsqu'elles apportent une frontiere utile.
- Garder les regles metier dans le backend ; le frontend ne doit pas etre la source de verite des calculs.
- Utiliser PostgreSQL comme source de verite transactionnelle.
- Assurer le multi-tenant par un `organization_id` obligatoire sur toutes les donnees metier.
- Ne jamais accepter l'identifiant d'organisation du client comme preuve d'autorisation ; le deduire du principal authentifie.
- Privilegier des operations idempotentes pour les traitements planifies et la generation d'echeances.
- Eviter les abstractions prematurees et les dependances inutiles.

Voir `docs/ARCHITECTURE.md` pour les decisions detaillees.

## 5. Conventions metier Senegal

- Devise par defaut : franc CFA BCEAO, code `XOF`, sans decimales dans le MVP.
- Stocker les montants en entier (`BIGINT` en base, `long`/type valeur en Java), jamais en virgule flottante.
- Fuseau metier par defaut : `Africa/Dakar`.
- Stocker les instants techniques en UTC et convertir pour l'affichage.
- Utiliser `LocalDate` pour les dates civiles telles que debut de bail, echeance et date de paiement.
- Langue de l'interface MVP : francais.
- Numeros de telephone normalises au format E.164, notamment `+221XXXXXXXXX` pour le Senegal.
- Formats d'adresse suffisamment souples pour Dakar et les autres regions : adresse libre, ville/localite, region et indication complementaire.
- Une relance WhatsApp est preparee et ouverte avec consentement humain ; elle n'est pas envoyee automatiquement dans le MVP.

## 6. Regles fonctionnelles communes

- Une organisation ne peut consulter ou modifier que ses propres donnees.
- Une unite appartient a un seul bien ; un bien appartient a un proprietaire dans le MVP.
- Une unite ne peut avoir qu'un bail actif couvrant une date donnee.
- Un bail reference un locataire principal, une unite, un loyer, une periodicite, un jour d'echeance et des dates de validite.
- Toute modification ayant un impact financier doit etre auditable.
- Les echeances conservent leur montant historique meme si le loyer du bail change ensuite.
- Un paiement peut regler totalement ou partiellement une echeance ; le statut est derive des montants dus et payes.
- Une quittance n'est disponible que pour une echeance integralement payee. Un paiement partiel produit au besoin un recu, hors MVP sauf arbitrage contraire.
- Les suppressions de donnees referencees sont interdites ou remplacees par un archivage/desactivation.
- Les calculs d'impayes doivent etre deterministes et testables a une date de reference donnee.

## 7. Securite et confidentialite

- Hasher les mots de passe avec Argon2id ou BCrypt configure avec un cout adapte.
- Ne jamais journaliser mot de passe, JWT, secret, contenu complet de document d'identite ou donnee personnelle non necessaire.
- Valider toutes les entrees cote backend et encoder les sorties selon leur contexte.
- Appliquer le moindre privilege. Roles MVP recommandes : `ADMIN` et `GESTIONNAIRE`.
- Proteger chaque acces par l'organisation et, si necessaire, par le role.
- Garder les secrets hors du depot ; fournir uniquement des variables d'environnement documentees et des valeurs locales non sensibles.
- Limiter la duree de vie des jetons et definir clairement la strategie de renouvellement/revocation.
- Les PDF et exports doivent appliquer les memes controles d'acces que les donnees sources.
- Toute dependance ajoutee doit etre maintenue, justifiee et analysee pour les vulnerabilites connues.

## 8. API et erreurs

- Prefixer l'API par `/api/v1`.
- Employer des noms de ressources au pluriel et des verbes HTTP conformes a leur semantique.
- Utiliser des DTO d'entree/sortie ; ne pas exposer directement les entites de persistance.
- Retourner des erreurs coherentes au format Problem Details (`application/problem+json`, RFC 9457).
- Valider pagination, tri et filtres ; imposer une taille maximale de page.
- Documenter les contrats avec OpenAPI et fournir des exemples utiles.
- Les changements incompatibles exigent une nouvelle version d'API ou une migration coordonnee.

## 9. Base de donnees et migrations

- Toute evolution de schema passe par une migration Flyway versionnee.
- Ne jamais modifier une migration deja partagee ou executee dans un environnement commun ; ajouter une nouvelle migration.
- Utiliser des contraintes `NOT NULL`, cles et index pour faire respecter les invariants importants.
- Inclure `organization_id` dans les contraintes d'unicite lorsque l'unicite est propre a un tenant.
- Utiliser des identifiants UUID generes par l'application ou la base selon une convention unique.
- Nommer tables et colonnes en `snake_case`.
- Ajouter des index sur les cles etrangeres et les recherches frequentes, sans indexer aveuglement.
- Prevoir `created_at`, `updated_at`, et lorsque pertinent `created_by`, `updated_by`.

## 10. Qualite et tests

- Backend : tests unitaires des regles metier, tests d'integration avec PostgreSQL/Testcontainers pour persistance, securite et migrations.
- Frontend : tests unitaires des composants et logique critique, tests d'integration des parcours principaux.
- End-to-end : au minimum connexion, creation du referentiel, bail, paiement et quittance.
- Tester explicitement l'isolation multi-tenant, les droits, paiements partiels, doublons, dates limites et generation PDF.
- Un correctif de bug doit inclure un test qui reproduit la regression lorsque cela est raisonnable.
- Aucun test ne doit dependre de l'heure courante non injectable ou de l'ordre d'execution.

## 11. Observabilite

- Produire des logs structures et exploitables, avec identifiant de correlation.
- Ne pas exposer de donnees sensibles dans les logs ou traces.
- Fournir des endpoints de sante et readiness via Spring Boot Actuator, proteges selon l'environnement.
- Journaliser les actions sensibles : connexion, creation/modification de bail, paiement, annulation et generation de quittance.
- Prevoir des metriques minimales sur erreurs HTTP, latence et traitements planifies.

## 12. Methode de travail des agents

Chaque agent doit :

1. Lire `AGENTS.md`, son fichier dans `agents/`, `docs/BACKLOG.md` et `docs/ARCHITECTURE.md` avant d'agir.
2. Verifier le code et les conventions existantes avant de proposer une structure nouvelle.
3. Identifier la user story et les criteres d'acceptation concernes.
4. Garder ses changements limites au besoin demande.
5. Signaler les hypotheses, risques, decisions et dependances inter-agents.
6. Ajouter ou mettre a jour les tests et la documentation necessaires.
7. Ne pas contourner une exigence de securite, de multi-tenancy ou d'audit pour accelerer une livraison.
8. Demander un arbitrage au Product Owner pour toute ambiguite fonctionnelle affectant les donnees ou le comportement utilisateur.
9. Demander une decision a l'Architecte pour tout changement transversal, nouveau service ou nouvelle dependance structurante.

## 13. Responsabilite des agents

- Orchestrateur : planification, coordination, dependances et validation globale.
- Product Owner : valeur, perimetre, priorites et acceptation fonctionnelle.
- Architecte : architecture, contrats transverses et decisions techniques.
- Backend : API, logique metier, securite serveur et integrations.
- Frontend : interface, parcours utilisateur, accessibilite et integration API.
- Database : modele relationnel, migrations, contraintes et performance SQL.
- Security : modele de menace, controles d'acces, secrets et revue de securite.
- QA : strategie de test, scenarios, non-regression et preuves de conformite.
- DevOps : conteneurs, CI/CD, configuration et exploitabilite.
- Documentation : coherence et maintien de la documentation produit et technique.

Les details de chaque role se trouvent dans `agents/`.

## 14. Definition of Ready

Une story est prete lorsqu'elle possede :

- un objectif utilisateur clair ;
- des criteres d'acceptation observables ;
- les regles metier et cas limites connus ;
- les dependances identifiees ;
- une decision UX ou un contrat API suffisant pour commencer ;
- aucune ambiguite bloquante sur les droits ou l'isolation des donnees.

## 15. Definition of Done

Une story est terminee lorsque :

- tous les criteres d'acceptation sont satisfaits ;
- le code compile et respecte les conventions du depot ;
- les tests pertinents passent ;
- les controles d'autorisation et de tenant sont testes ;
- les migrations sont reproductibles depuis une base vide, si le schema change ;
- l'API/OpenAPI et la documentation sont a jour ;
- les erreurs et logs sont exploitables sans fuite de donnees ;
- la revue QA et les revues specialisees requises sont realisees ;
- aucun secret, artefact local ou donnees personnelles de test ne sont commites.

## 16. Regles Git et livraisons

- Une branche et une pull request par unite de travail coherente.
- Commits petits, explicites et sans melange de refactorisation non liee.
- Ne pas casser la branche principale ; la CI doit etre verte avant integration.
- Toute decision structurante doit etre capturee dans `docs/ARCHITECTURE.md` ou dans un ADR futur.
- Les donnees de demonstration doivent etre fictives et clairement identifiees.
