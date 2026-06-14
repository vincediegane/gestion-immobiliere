# Backlog MVP - Gestion immobiliere Senegal

## 1. Objectif

Livrer un MVP permettant a une organisation de gestion immobiliere de tenir son referentiel, contractualiser une location, suivre les loyers, identifier les impayes, preparer une relance WhatsApp et produire une quittance PDF.

## 2. Personas

- **Administrateur d'organisation** : configure l'organisation, gere les utilisateurs et dispose de tous les droits metier.
- **Gestionnaire immobilier** : gere le referentiel, les baux, echeances, paiements, relances et quittances de son organisation.

## 3. Priorites

- **P0** : indispensable pour exploiter le MVP.
- **P1** : necessaire pour une experience MVP complete, livrable apres le coeur transactionnel.
- **P2** : amelioration utile, a prendre seulement si les P0/P1 sont stabilisees.

Ordre conseille : E0, E1, E2, E3, E4, E5, E6, E7, E8, E9.

## Epic E0 - Socle technique et environnement

### US-E0-01 - Demarrer l'environnement local [P0]

**En tant que** developpeur, **je veux** demarrer les composants avec Docker Compose **afin de** disposer d'un environnement reproductible.

**Criteres d'acceptation**

- Docker Compose definit PostgreSQL et, lorsque leurs projets existent, le backend et le frontend.
- Les services disposent de healthchecks pertinents et de variables d'environnement documentees.
- Aucun secret reel n'est versionne.
- Une base vide est initialisee automatiquement par Flyway.
- Une commande documentee permet de demarrer et d'arreter l'environnement.

### US-E0-02 - Executer la qualite en integration continue [P0]

**En tant que** equipe, **je veux** une CI automatique **afin de** detecter les regressions avant integration.

**Criteres d'acceptation**

- La CI compile backend et frontend.
- Elle execute les tests, controles de format/lint et migrations sur une base PostgreSQL propre.
- Un echec bloque l'integration.
- Les artefacts de build ne contiennent ni secret ni fichier local inutile.

### US-E0-03 - Exposer l'etat de sante [P1]

**En tant qu'** exploitant, **je veux** connaitre l'etat du backend **afin de** diagnostiquer un service indisponible.

**Criteres d'acceptation**

- Des controles de vitalite et readiness sont disponibles.
- La readiness signale l'indisponibilite de PostgreSQL.
- Les details sensibles ne sont pas exposes publiquement.

## Epic E1 - Organisation, authentification et utilisateurs

### US-E1-01 - Initialiser une organisation et son administrateur [P0]

**En tant qu'** operateur de la plateforme, **je veux** creer une organisation avec un administrateur **afin de** permettre sa premiere connexion.

**Criteres d'acceptation**

- Une organisation possede au minimum un nom, un identifiant unique et un statut actif/inactif.
- L'administrateur est rattache a une seule organisation dans le MVP.
- Son email est unique sans tenir compte de la casse.
- Le mot de passe n'est jamais stocke en clair.
- Le mecanisme d'initialisation ne permet pas une inscription publique non controlee.

### US-E1-02 - Se connecter [P0]

**En tant qu'** utilisateur actif, **je veux** me connecter **afin d'** acceder aux donnees de mon organisation.

**Criteres d'acceptation**

- Des identifiants valides retournent une session JWT conforme a la strategie retenue.
- Des identifiants invalides retournent une erreur generique sans reveler l'existence du compte.
- Un utilisateur ou une organisation inactive ne peut pas se connecter.
- Les tentatives echouees sont journalisees sans mot de passe ni jeton.
- Les endpoints metier refusent une requete sans jeton valide.

### US-E1-03 - Gerer les utilisateurs de l'organisation [P1]

**En tant qu'** administrateur, **je veux** creer, lister, modifier et desactiver des utilisateurs **afin de** controler l'acces de mon equipe.

**Criteres d'acceptation**

- L'administrateur peut attribuer `ADMIN` ou `GESTIONNAIRE`.
- Il ne peut agir que sur les utilisateurs de son organisation.
- Un gestionnaire ne peut pas administrer les comptes.
- La desactivation invalide l'acces futur selon la strategie de jeton documentee.
- Il est impossible de retirer le dernier administrateur actif sans transfert prealable.

### US-E1-04 - Garantir l'isolation multi-tenant [P0]

**En tant qu'** organisation, **je veux** que mes donnees soient isolees **afin de** proteger mes clients et mes operations.

**Criteres d'acceptation**

- L'organisation est deduite du principal authentifie, jamais d'un parametre de confiance fourni par le client.
- Une ressource d'une autre organisation est inaccessible en lecture, modification, suppression et telechargement.
- Les listes, recherches, compteurs et exports sont filtres par organisation.
- Des tests automatises couvrent au moins deux organisations pour chaque famille de ressource sensible.

## Epic E2 - Proprietaires

### US-E2-01 - Creer un proprietaire [P0]

**En tant que** gestionnaire, **je veux** enregistrer un proprietaire **afin de** rattacher ses biens.

**Criteres d'acceptation**

- Le nom complet ou la raison sociale est obligatoire.
- Le telephone accepte un format normalise E.164 ; email et adresse sont facultatifs.
- Le proprietaire est automatiquement rattache a l'organisation connectee.
- Les erreurs de validation identifient les champs concernes.
- Un proprietaire cree apparait dans la liste de l'organisation seulement.

### US-E2-02 - Rechercher et consulter les proprietaires [P0]

**En tant que** gestionnaire, **je veux** rechercher un proprietaire **afin de** retrouver rapidement son portefeuille.

**Criteres d'acceptation**

- La liste est paginee et filtrable au minimum par nom et telephone.
- La fiche affiche les coordonnees et les biens rattaches.
- Les etats chargement, aucun resultat et erreur sont visibles.

### US-E2-03 - Modifier ou archiver un proprietaire [P1]

**En tant que** gestionnaire, **je veux** maintenir les informations d'un proprietaire **afin de** garder un referentiel fiable.

**Criteres d'acceptation**

- Les memes validations que la creation s'appliquent.
- Un proprietaire reference par un bien n'est pas supprime physiquement.
- Un proprietaire archive reste visible dans l'historique mais n'est plus propose par defaut pour un nouveau bien.
- La modification est datee et attribuable a un utilisateur.

## Epic E3 - Biens et unites locatives

### US-E3-01 - Creer un bien immobilier [P0]

**En tant que** gestionnaire, **je veux** creer un bien rattache a un proprietaire **afin de** structurer le patrimoine gere.

**Criteres d'acceptation**

- Le bien contient un libelle, un type, un proprietaire, une adresse/localite et un statut.
- Le proprietaire appartient a la meme organisation.
- Le bien ne peut etre cree pour un proprietaire archive sans confirmation/regle explicite.
- La fiche du bien affiche son proprietaire et ses unites.

### US-E3-02 - Gerer les unites d'un bien [P0]

**En tant que** gestionnaire, **je veux** creer et modifier des unites **afin de** louer separement les appartements, boutiques ou locaux.

**Criteres d'acceptation**

- Une unite possede un libelle unique dans son bien, un type et un statut.
- Elle appartient a un seul bien de la meme organisation.
- La surface, le nombre de pieces et une description sont facultatifs.
- Son statut d'occupation affiche est coherent avec l'existence d'un bail actif.
- Une unite avec historique financier ne peut pas etre supprimee physiquement.

### US-E3-03 - Lister et filtrer le parc [P1]

**En tant que** gestionnaire, **je veux** filtrer biens et unites **afin de** voir rapidement les locaux libres ou occupes.

**Criteres d'acceptation**

- La liste est paginee et filtrable par proprietaire, localite, type et occupation.
- Le resultat affiche au minimum le bien, l'unite, le proprietaire et le statut.
- Le statut est derive de donnees serveur et non calcule uniquement dans le navigateur.

## Epic E4 - Locataires

### US-E4-01 - Creer un locataire [P0]

**En tant que** gestionnaire, **je veux** enregistrer un locataire **afin de** preparer son bail et son suivi.

**Criteres d'acceptation**

- Le nom complet et un telephone principal valide sont obligatoires.
- Email, adresse, type et reference de piece d'identite sont facultatifs.
- Les donnees d'identite sont minimisees et protegees dans les logs et erreurs.
- Le locataire est limite a l'organisation connectee.

### US-E4-02 - Rechercher et consulter les locataires [P0]

**En tant que** gestionnaire, **je veux** retrouver un locataire **afin de** consulter ses baux et sa situation.

**Criteres d'acceptation**

- La recherche accepte nom et telephone et retourne une liste paginee.
- La fiche affiche coordonnees, baux et situation courante sans exposer de donnees inutiles.
- Un utilisateur ne peut pas consulter un locataire d'une autre organisation.

### US-E4-03 - Modifier ou archiver un locataire [P1]

**En tant que** gestionnaire, **je veux** mettre a jour un locataire **afin de** conserver des coordonnees correctes.

**Criteres d'acceptation**

- Les validations de creation restent appliquees.
- L'archivage conserve les baux, paiements et quittances historiques.
- Un locataire archive n'est plus propose par defaut pour un nouveau bail.

## Epic E5 - Contrats de bail

### US-E5-01 - Creer un bail [P0]

**En tant que** gestionnaire, **je veux** creer un bail entre un locataire et une unite **afin de** definir les obligations locatives.

**Criteres d'acceptation**

- Le bail exige une unite, un locataire, une date de debut, un loyer XOF positif, une periodicite mensuelle et un jour d'echeance valide.
- La date de fin est facultative mais ne peut preceder la date de debut.
- Locataire et unite appartiennent a la meme organisation.
- Le bail est refuse s'il chevauche un autre bail actif de l'unite.
- Le montant, les dates et le jour d'echeance sont confirmes avant activation.
- L'activation cree ou planifie les echeances sans doublon selon la regle documentee.

### US-E5-02 - Consulter un bail et son echeancier [P0]

**En tant que** gestionnaire, **je veux** consulter un bail **afin de** comprendre ses conditions et son historique financier.

**Criteres d'acceptation**

- La fiche affiche locataire, unite, bien, proprietaire, dates, montant, statut et echeances.
- Chaque echeance affiche periode, date limite, montant du, montant paye et statut.
- Les donnees historiques restent identiques si le referentiel est modifie.

### US-E5-03 - Resilier ou terminer un bail [P0]

**En tant que** gestionnaire, **je veux** saisir la fin effective d'un bail **afin de** liberer l'unite et arreter les futures obligations.

**Criteres d'acceptation**

- La date de fin effective ne peut preceder le debut du bail.
- Les echeances deja dues et paiements sont conserves.
- Les echeances futures posterieures a la fin sont annulees ou ajustees selon une regle explicite et auditee.
- L'unite redevient disponible apres la date de fin si aucun autre bail ne la couvre.

### US-E5-04 - Modifier les conditions futures d'un bail [P1]

**En tant que** gestionnaire, **je veux** modifier le loyer ou l'echeance a une date d'effet **afin de** gerer une evolution contractuelle sans alterer l'historique.

**Criteres d'acceptation**

- La modification exige une date d'effet et une confirmation.
- Les echeances passees ou deja payees ne changent pas.
- Les futures echeances sont regenerees ou ajustees sans doublon.
- L'ancienne et la nouvelle valeur restent auditables.

## Epic E6 - Echeances, loyers et paiements

### US-E6-01 - Generer les echeances mensuelles [P0]

**En tant que** gestionnaire, **je veux** disposer automatiquement des echeances **afin de** suivre les loyers dus.

**Criteres d'acceptation**

- Une echeance est generee pour chaque periode couverte par un bail actif.
- La date limite respecte le jour d'echeance, y compris pour les mois plus courts selon une regle documentee.
- Le traitement peut etre rejoue sans creer de doublon.
- Le montant de l'echeance est fige lors de sa creation.
- Les erreurs sont journalisees et une execution partielle peut etre reprise sans corruption.

### US-E6-02 - Lister les loyers [P0]

**En tant que** gestionnaire, **je veux** consulter les echeances **afin de** voir ce qui est attendu, paye ou en retard.

**Criteres d'acceptation**

- La liste est paginee et filtrable par periode, statut, locataire, bien et proprietaire.
- Elle affiche date limite, montant du, montant paye, solde et statut.
- Les totaux affiches correspondent aux resultats filtres selon une semantique documentee.

### US-E6-03 - Enregistrer un paiement [P0]

**En tant que** gestionnaire, **je veux** enregistrer un paiement **afin de** mettre a jour la situation d'une echeance.

**Criteres d'acceptation**

- Le paiement exige une echeance, un montant XOF positif, une date, un mode et facultativement une reference/note.
- Un paiement partiel met l'echeance au statut `PARTIEL` et calcule le solde exact.
- Le cumul ne peut depasser le montant du sans traitement explicite de trop-percu, hors MVP.
- Un paiement complet met l'echeance au statut `PAYE`.
- Une soumission repetee avec la meme cle d'idempotence ne cree pas un second paiement.
- L'auteur et la date d'enregistrement sont audites.

### US-E6-04 - Corriger ou annuler un paiement [P1]

**En tant qu'** administrateur, **je veux** annuler un paiement errone **afin de** retablir une situation correcte sans effacer l'historique.

**Criteres d'acceptation**

- Un paiement n'est jamais supprime physiquement.
- L'annulation exige un motif et les droits `ADMIN`.
- Le montant paye, le solde et le statut de l'echeance sont recalcules transactionnellement.
- Une quittance liee est invalidee ou regeneree selon une regle explicite.
- L'action est auditee.

## Epic E7 - Impayes et tableau de bord

### US-E7-01 - Detecter les echeances en retard [P0]

**En tant que** gestionnaire, **je veux** identifier automatiquement les loyers impayes **afin de** agir rapidement.

**Criteres d'acceptation**

- Une echeance avec solde positif devient `EN_RETARD` lorsque sa date limite est anterieure a la date metier de reference.
- Une echeance partiellement payee et echue est egalement en retard avec son solde restant.
- Une echeance payee integralement n'est jamais consideree en retard.
- Le calcul est idempotent, testable avec une horloge controlee et limite a l'organisation.
- Le retour a un statut correct apres paiement ou annulation est automatique.

### US-E7-02 - Consulter le tableau de bord [P1]

**En tant que** gestionnaire, **je veux** voir les indicateurs essentiels **afin de** prioriser ma journee.

**Criteres d'acceptation**

- Le tableau de bord affiche pour une periode le montant attendu, encaisse et restant.
- Il affiche le nombre et le montant des echeances en retard.
- Il propose un acces direct aux listes filtrees correspondantes.
- Les indicateurs sont calcules cote serveur et limites a l'organisation.
- Une definition visible ou documentee precise la periode et les statuts inclus.

### US-E7-03 - Consulter une balance locataire [P1]

**En tant que** gestionnaire, **je veux** consulter la situation d'un locataire **afin de** expliquer son solde.

**Criteres d'acceptation**

- La vue liste chronologiquement echeances, paiements, annulations et solde.
- Le total est coherent avec les echeances du bail selectionne.
- Les operations archivees restent visibles et clairement identifiees.

## Epic E8 - Relances WhatsApp

### US-E8-01 - Preparer un message de relance [P0]

**En tant que** gestionnaire, **je veux** generer une relance pour un impaye **afin de** contacter rapidement le locataire.

**Criteres d'acceptation**

- L'action est disponible uniquement pour une echeance avec solde positif.
- Le message propose contient au minimum le nom du locataire, la periode, le solde XOF et la date d'echeance.
- Le numero est normalise et valide avant generation du lien.
- L'utilisateur peut relire et modifier le message avant ouverture de WhatsApp.
- Le backend ou frontend genere un lien `https://wa.me/<numero>?text=<message_encode>` correctement encode.
- Aucun message n'est envoye automatiquement par la plateforme.

### US-E8-02 - Tracer la preparation d'une relance [P1]

**En tant que** gestionnaire, **je veux** enregistrer qu'une relance a ete preparee **afin de** coordonner le suivi.

**Criteres d'acceptation**

- L'historique conserve date, utilisateur, echeance, canal et modele/message final selon la politique de minimisation.
- La trace ne pretend pas que le message a ete livre ou lu.
- L'historique est visible depuis l'echeance ou le locataire.
- Seule l'organisation concernee peut le consulter.

## Epic E9 - Quittances PDF

### US-E9-01 - Generer une quittance [P0]

**En tant que** gestionnaire, **je veux** generer une quittance PDF **afin de** remettre une preuve de paiement au locataire.

**Criteres d'acceptation**

- La generation est autorisee uniquement pour une echeance integralement payee et non annulee.
- La quittance contient un numero unique, l'organisation/gestionnaire, le proprietaire, le locataire, le bien/unite, la periode, le montant et la date de paiement.
- Les montants sont affiches sans ambiguite en XOF.
- Le PDF est lisible, non vide et porte un nom de fichier stable et sur.
- La generation repetee retourne le meme document logique ou une version controlee, sans numerotation multiple accidentelle.
- Le document est inaccessible a une autre organisation.

### US-E9-02 - Telecharger une quittance existante [P0]

**En tant que** gestionnaire, **je veux** retrouver une quittance **afin de** la remettre a nouveau sans recreer son historique.

**Criteres d'acceptation**

- La quittance est accessible depuis l'echeance, le bail ou le locataire.
- Le telechargement applique authentification, autorisation et controle tenant.
- Le serveur retourne un type `application/pdf` et des en-tetes de telechargement corrects.
- Une quittance invalidee n'est pas presentee comme valide.

## Epic E10 - Recherche, audit et finition MVP

### US-E10-01 - Disposer d'une recherche coherente [P1]

**En tant que** gestionnaire, **je veux** des listes paginees et filtrables **afin de** travailler efficacement avec un portefeuille croissant.

**Criteres d'acceptation**

- Toutes les listes importantes utilisent une pagination bornee.
- Les tris autorises sont explicites et valides cote serveur.
- Les filtres vides ou invalides ont un comportement coherent.
- Les performances restent acceptables sur un jeu de donnees MVP defini par QA.

### US-E10-02 - Auditer les actions sensibles [P1]

**En tant qu'** administrateur, **je veux** savoir qui a effectue une action financiere ou contractuelle **afin de** traiter les erreurs et litiges.

**Criteres d'acceptation**

- Creations/modifications de bail, paiements, annulations, relances et quittances produisent une trace.
- La trace contient organisation, acteur, action, cible et instant, sans secret ni donnee personnelle excessive.
- Les traces ne sont pas modifiables par un gestionnaire.
- Une politique de retention est documentee avant production.

### US-E10-03 - Valider le parcours MVP de bout en bout [P0]

**En tant que** Product Owner, **je veux** une preuve du parcours complet **afin de** accepter le MVP.

**Criteres d'acceptation**

- Un test ou scenario reproductible couvre connexion, proprietaire, bien, unite, locataire, bail, echeance, paiement et quittance.
- Un second scenario couvre retard, paiement partiel et preparation WhatsApp.
- Un scenario negatif prouve l'isolation entre deux organisations.
- Toutes les stories P0 sont acceptees et aucun defaut critique ou eleve non arbitre ne reste ouvert.
- La documentation de demarrage et d'exploitation correspond a la version livree.

## 4. Hors backlog MVP

- Paiement Wave, Orange Money, carte ou rapprochement bancaire.
- Envoi WhatsApp via API Business, SMS ou email automatique.
- Comptabilite, commissions de gestion et reversements proprietaires.
- Depot de garantie avance, penalites complexes et indexation automatique.
- Colocation multi-signataires et repartition avancee des charges.
- Etats des lieux, maintenance, tickets et fournisseurs.
- Portails externes, application mobile et mode hors ligne.
- Abonnements SaaS, facturation de l'organisation et quotas commerciaux.
- Import/export de masse, sauf ajout ulterieur priorise.

## 5. Questions produit a arbitrer avant implementation des stories concernees

- Regle exacte de premiere echeance lorsque le bail commence en cours de mois : mois complet ou prorata.
- Regle de date limite pour un jour d'echeance absent d'un mois : dernier jour du mois recommande.
- Traitement d'une fin de bail en cours de mois et d'une eventuelle proratisation.
- Strategie JWT detaillee : access token seul a courte duree ou access/refresh token avec rotation.
- Contenu legal et numerotation finale des quittances au Senegal.
- Niveau de conservation du texte complet des relances au regard de la minimisation des donnees.
- Modes de paiement autorises et references obligatoires selon le mode.
