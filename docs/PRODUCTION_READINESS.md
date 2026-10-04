# Production readiness — Quantum Deep Calm AI

Ce document sépare volontairement les preuves fournies par le dépôt des preuves externes nécessaires à une publication commerciale.

## 1. Gates logiciels obligatoires

Une révision candidate ne peut pas être qualifiée de prête tant que les contrôles suivants ne sont pas tous verts sur le SHA exact à publier :

- Repository gate ;
- Repo Guard ;
- Android Lint debug et release ;
- tests unitaires avec vérification qu’au moins un test a réellement été exécuté ;
- génération APK debug ;
- génération AAB release ;
- smoke test Android sur appareil géré ;
- CodeQL Java/Kotlin ;
- GitGuardian ;
- Android Autoloop sur `main` sans correction résiduelle ni changement non versionné.

Aucun baseline Lint global ou contournement silencieux ne doit transformer une erreur en faux vert. Une suppression Lint ciblée n’est acceptable que lorsqu’elle correspond à un contrat de plateforme documenté et qu’une garde équivalente est implémentée dans le code ; c’est le cas du `MediaSessionService` exporté pour les contrôles média système, dont les connexions sont filtrées par `onGetSession()`.

## 2. Parcours produit commercial minimal

Le cœur commercial offline doit vérifier de bout en bout :

- chaque session visible possède un identifiant stable ;
- chaque session produit un profil audio local distinct et déterministe ;
- Accueil peut lancer la session choisie ;
- Bibliothèque persiste les favoris et peut les relancer ;
- Sommeil propose uniquement des sessions réellement jouables et ne promet aucun effet médical ou physiologique ;
- Lecteur affiche la session réellement chargée et fournit lecture, pause et remise au début ;
- Progression augmente uniquement après confirmation de lecture réelle par Media3 et conserve la dernière session réellement lue ;
- Confidentialité est accessible dans l’application ;
- aucune fonction visible ne dépend d’un service réseau inexistant.

## 3. Architecture confidentialité actuelle

Le build Android commercial visé :

- ne demande pas la permission Internet ;
- ne crée pas de compte utilisateur ;
- ne contient pas d’analytics distant ni de publicité ;
- conserve favoris et progression dans le stockage privé local ;
- désactive les sauvegardes applicatives ;
- génère les ambiances sur l’appareil au lieu de télécharger un catalogue ;
- expose `MediaSessionService` comme requis par Media3 pour les contrôles média système, mais refuse les contrôleurs qui ne sont ni l’application elle-même ni reconnus fiables par Media3.

Toute future dépendance réseau, publicité, analytics, compte ou paiement intégré invalide cette section et exige une nouvelle revue Data safety / confidentialité.

## 4. Preuves externes encore nécessaires avant production

Ces éléments ne peuvent pas être fabriqués par la CI du dépôt :

- [ ] Les quatre secrets d’upload Google Play sont configurés correctement dans GitHub Actions.
- [ ] `Play Release Bundle` réussit depuis `main` avec la vraie clé d’upload.
- [ ] `jarsigner -verify -strict` réussit sur l’AAB final.
- [ ] L’AAB signé est accepté dans Google Play Console.
- [ ] Le package `com.quantumdeepcalm.ai` est enregistré/vérifié dans le compte développeur approprié.
- [ ] La section Data safety est remplie et cohérente avec le build distribué.
- [ ] Une politique de confidentialité est accessible sur une URL publique active, non géobloquée et référencée dans Play Console.
- [ ] Les déclarations Publicités, Audience cible, Classification du contenu et Accès à l’application sont complétées dans Play Console.
- [ ] Le prix, les pays de distribution et le statut gratuit/payant sont décidés dans Play Console.
- [ ] Les assets Store Listing finaux sont fournis : icône, captures, graphique de présentation si requis, descriptions et coordonnées de support.
- [ ] Si le compte personnel a été créé après le 13 novembre 2023, les exigences Google Play de test fermé et d’accès production sont satisfaites.
- [ ] La vérification d’accès à un appareil Android réel du compte développeur est satisfaite si Google Play la demande.
- [ ] Test physique final sur au moins un téléphone Android réel : installation, premier lancement, toutes les sessions, arrière-plan, reprise, audio, contrôles média système, suppression/réinstallation.
- [ ] La branche `main` est protégée par un ruleset/branch protection réellement vérifiable : PR obligatoire, checks obligatoires, force-push et suppression bloqués.

## 5. Critère de sortie

Le statut « production commerciale » n’est autorisé que lorsque les sections 1 et 2 sont prouvées sur le SHA final et que toutes les cases applicables de la section 4 sont fermées par une preuve externe.
