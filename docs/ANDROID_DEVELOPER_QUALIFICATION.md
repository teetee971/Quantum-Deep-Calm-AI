# Qualification développeur Android

## Principe

Quantum Deep Calm AI applique une qualification **emulator-first**.

L’émulateur Android géré par Gradle est le portail principal de qualification développeur. Une validation manuelle du propriétaire ne doit pas répéter un contrôle que la CI peut mesurer de manière déterministe.

## Politique SDK

Le produit utilise actuellement :

- `minSdk = 26` : plus ancienne API officiellement supportée ;
- `targetSdk = 37` : comportement cible déclaré au système ;
- `compileSdk = 37` : API utilisée pour compiler l’application.

Le banc de qualification exécute la suite instrumentée sur quatre niveaux :

- **API 26** : plancher réel du produit ;
- **API 29** : point de contrôle intermédiaire/legacy ;
- **API 36** : comportement Android moderne ;
- **API 37** : compatibilité avec la version Android la plus récente ciblée par le projet, avec pages mémoire 16 KB forcées.

API 24 n’est pas utilisée tant que `minSdk` reste à 26. Tester API 24 serait incohérent : l’application n’est pas censée s’y installer. Si le produit doit officiellement supporter API 24, la baisse de `minSdk` doit faire l’objet d’un changement séparé avec audit de compatibilité et qualification dédiée.

## Particularités du banc

- Gradle Managed Devices bloque par défaut les API 26 et inférieures. Le dépôt active explicitement `android.experimental.testOptions.managedDevices.allowOldApiLevelDevices=true` afin de tester le plancher produit API 26 au lieu de le contourner.
- API 26 / 29 / 36 utilisent les images AOSP x86_64.
- API 37 utilise l’image Google APIs x86_64 publiée pour Android 17 (`android-37.0`), car l’image `default` n’est pas publiée sous la même forme pour ce niveau.
- Le provisioning API 37 autorise le canal SDK preview requis par la publication courante de cette image.
- Le Managed Device API 37 force `FORCE_16KB_PAGES` afin que le test récent couvre aussi la compatibilité mémoire 16 KB.
- Le harness UI épingle `androidx.test.espresso:espresso-core:3.7.0`, version qui n’utilise plus l’accès réflexif historique à `InputManager.getInstance()` incompatible avec Android 17.

## Gate principal

Le job GitHub Actions `Android developer qualification` n’est vert que si les trois niveaux suivants sont verts :

1. `Repository gate`
   - intégrité du dépôt ;
   - absence de placeholders interdits ;
   - absence de marqueurs de dette dans `app/src/main` ;
   - `git diff --check` ;
   - `minSdk = 26` cohérent avec le banc ;
   - `targetSdk` conforme.

2. `Android build + Play bundle`
   - structure Gradle attendue ;
   - Lint debug et release ;
   - tests unitaires ;
   - vérification qu’au moins un test a réellement été exécuté ;
   - APK debug ;
   - AAB release ;
   - compilation `compileSdk/targetSdk 37`.

3. `Emulator qualification`
   - matrice API 26 / 29 / 36 / 37 ;
   - KVM obligatoire ;
   - image Android gérée pour chaque niveau ;
   - API 37 exécutée avec pages mémoire 16 KB ;
   - exécution de la suite `androidTest` complète sur chaque API ;
   - vérification indépendante des rapports JUnit ;
   - refus du faux vert si zéro test est exécuté ;
   - échec global si une seule API échoue ;
   - conservation des rapports d’instrumentation comme artefacts de preuve lorsqu’ils existent.

## Ce que l’émulateur qualifie actuellement

Les tests instrumentés couvrent notamment le démarrage de l’application, le lecteur de méditation hors ligne, la sélection réelle des sessions, les parcours Sommeil, les favoris/bibliothèque et la persistance des données testées par les repositories instrumentés.

Toute nouvelle fonctionnalité Android observable doit ajouter ou étendre un test instrumenté lorsque cela est techniquement possible.

## Validation humaine résiduelle

Une validation manuelle reste légitime uniquement lorsqu’une propriété ne peut pas être prouvée honnêtement par l’émulateur ou la CI, par exemple :

- comportement propre à un constructeur/OEM ;
- périphérique ou capteur physique non émulé ;
- comportement audio matériel dépendant du terminal ;
- ergonomie subjective finale sur appareils représentatifs ;
- décision commerciale de publication ;
- gestion des secrets et signature de production via le workflow de release.

Cette validation physique ne doit pas être transformée en gate générique pour les changements qui ne touchent pas ces surfaces.

## Niveaux de vérité

- **CI build verte** : le code compile et satisfait les contrôles statiques concernés.
- **Qualification développeur verte** : build + tests + matrice émulateur API 26/29/36/37 sont verts.
- **Validation physique** : complément ciblé lorsqu’un matériel réel est nécessaire.
- **Release commerciale** : artefact signé et processus de publication explicitement exécuté.

Aucun niveau ne doit être présenté comme le niveau suivant sans preuve correspondante.
