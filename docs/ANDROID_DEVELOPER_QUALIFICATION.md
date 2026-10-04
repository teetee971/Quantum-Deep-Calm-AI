# Qualification développeur Android

## Principe

Quantum Deep Calm AI applique une qualification **emulator-first**.

L’émulateur Android géré par Gradle est le portail principal de qualification développeur. Une validation manuelle du propriétaire ne doit pas répéter un contrôle que la CI peut mesurer de manière déterministe.

## Gate principal

Le job GitHub Actions `Android developer qualification` n’est vert que si les trois niveaux suivants sont verts :

1. `Repository gate`
   - intégrité du dépôt ;
   - absence de placeholders interdits ;
   - absence de marqueurs de dette dans `app/src/main` ;
   - `git diff --check` ;
   - `targetSdk` conforme.

2. `Android build + Play bundle`
   - structure Gradle attendue ;
   - Lint debug et release ;
   - tests unitaires ;
   - vérification qu’au moins un test a réellement été exécuté ;
   - APK debug ;
   - AAB release.

3. `Emulator qualification (API 35)`
   - KVM obligatoire ;
   - image Android gérée ;
   - exécution de la suite `androidTest` complète ;
   - vérification indépendante des rapports JUnit ;
   - refus du faux vert si zéro test est exécuté ;
   - conservation des rapports d’instrumentation comme artefacts de preuve.

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
- **Qualification développeur verte** : build + tests + émulateur instrumenté sont verts.
- **Validation physique** : complément ciblé lorsqu’un matériel réel est nécessaire.
- **Release commerciale** : artefact signé et processus de publication explicitement exécuté.

Aucun niveau ne doit être présenté comme le niveau suivant sans preuve correspondante.
