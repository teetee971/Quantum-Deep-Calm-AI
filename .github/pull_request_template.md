## Checklist de conformité – Pull Request

Cette checklist doit refléter l’état réel de la PR. Une case cochée signifie qu’une preuve existe dans le code, les tests ou la CI.

### Conformité du dépôt
- [ ] Aucun fichier Git LFS ni pointeur LFS ajouté.
- [ ] Aucun fichier interdit, archive, source de design ou média lourd ajouté au dépôt.
- [ ] Aucun dossier temporaire, historique ou résidu de build versionné.
- [ ] Aucun marqueur `TODO`, `FIXME`, `HACK` ou `XXX` ajouté dans `app/src/main`.
- [ ] `git diff --check` est propre.

### Android et architecture
- [ ] La PR reste compatible avec la structure Android décrite dans `docs/ARCHITECTURE.md`.
- [ ] Le Gradle Wrapper versionné est utilisé ; aucune chaîne de build parallèle n’est introduite.
- [ ] Le `targetSdk` reste conforme au gate Google Play du dépôt.
- [ ] Les permissions, composants exportés et services Android modifiés ont été revus pour leur surface de sécurité.
- [ ] Les fonctionnalités non implémentées restent explicitement signalées comme telles dans l’UI et la documentation.

### Validation fonctionnelle
- [ ] Android Lint passe sans baseline ajoutée pour masquer de nouveaux défauts.
- [ ] Les tests unitaires exécutent au moins un test réel et passent.
- [ ] L’APK debug est généré.
- [ ] L’AAB release est généré.
- [ ] Le smoke test sur appareil Android géré passe si la PR touche au runtime ou à l’UI.
- [ ] Les changements fonctionnels sont couverts par un test ou par une justification de validation vérifiable.

### Sécurité et qualité
- [ ] Repo Guard est vert.
- [ ] CodeQL est vert.
- [ ] GitGuardian est vert.
- [ ] Aucun secret, keystore, mot de passe ou jeton n’est ajouté au dépôt.
- [ ] Aucune suppression globale de contrôle, aucun `lint-baseline.xml` et aucun contournement silencieux n’est introduit.
- [ ] Toute suppression Lint ciblée est justifiée par une contrainte Android documentée et compensée par une garde équivalente.

### Release
- [ ] La documentation est mise à jour si le comportement, les prérequis ou la procédure de release changent.
- [ ] Une PR ne déclare pas l’application « prête production » sur la seule base d’un build CI.
- [ ] Si une release signée est concernée, le workflow `Play Release Bundle` vérifie effectivement la signature de l’AAB.

### Validation finale
- [ ] Tous les checks obligatoires de la PR sont verts.
- [ ] Aucun état « opérationnel » ou « terminé » n’est revendiqué sans preuve.
- [ ] La PR peut être fusionnée sans dette connue non documentée.
