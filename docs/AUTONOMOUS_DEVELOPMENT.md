# Pipeline de développement autonome

## Objectif

Automatiser au maximum la progression du produit sans transformer `main` en branche auto-modifiable non contrôlée.

Le système sépare quatre responsabilités :

1. **Roadmap machine-readable** : `.github/autodev-roadmap.json` définit l'ordre produit et les critères d'acceptation.
2. **Contrôleur de travail** : `Autonomous Development Controller` maintient exactement un item actif et crée/reprend l'issue correspondante.
3. **Agent de développement** : Codex ou un autre agent connecté travaille l'issue sur une branche `autodev/*`, ajoute les tests et ouvre une PR.
4. **Qualification et fusion** : Android CI, émulateur, Repo Guard et CodeQL qualifient la PR. `Autodev Merge Controller` fusionne automatiquement seulement les changements non sensibles et entièrement verts.

## Boucle normale

```text
Roadmap
  -> issue AUTODEV
  -> agent de développement
  -> branche autodev/*
  -> pull request
  -> Repository gate
  -> Lint + tests unitaires + APK + AAB
  -> émulation Android complète
  -> Repo Guard
  -> CodeQL
  -> contrôle chemins sensibles
  -> squash merge automatique si tout est vert
  -> fermeture de l'issue
  -> item suivant
```

## Fréquences

- Le contrôleur de roadmap s'exécute toutes les 4 heures et à la demande.
- Le contrôleur de fusion vérifie les PR AUTODEV trois fois par heure et à la demande.
- Les builds lourds ne sont déclenchés que lorsqu'un agent pousse réellement une PR ou lorsqu'un workflow Android existant les requiert.

## Critère de complétion

Une issue fermée sans PR AUTODEV fusionnée n'est pas considérée comme terminée. Le contrôleur la rouvre.

Une PR n'est pas fusionnée automatiquement si un check requis manque, est encore en cours, échoue, est annulé ou si la tête de branche change pendant l'évaluation.

## Auto-merge fail-closed

Les PR de code produit peuvent être fusionnées automatiquement lorsqu'elles sont sûres et complètement qualifiées.

L'auto-merge est désactivé si la PR touche notamment :

- workflows GitHub Actions ;
- wrapper Gradle ;
- dépendances/build configuration ;
- manifeste Android et permissions ;
- sécurité réseau ;
- signature/release Play ;
- secrets ;
- éléments de confidentialité modifiant le périmètre réglementaire.

Cela évite qu'un agent puisse affaiblir les contrôles qui doivent justement le superviser.

## Limite volontaire

GitHub Actions n'est pas utilisé comme générateur arbitraire de code. La production de code est déléguée à un agent de développement connecté. Le pipeline fournit à cet agent une file de travail, un contrat et une sortie automatique lorsque les preuves sont suffisantes.

Sans agent connecté, le système continuera à surveiller le dépôt et à préparer l'issue suivante, mais il n'inventera pas de code de fonctionnalité.

## Publication commerciale

La fusion automatique d'une fonctionnalité ne signifie pas publication Google Play. La chaîne `Play Release Bundle`, la signature réelle, Play Console, Data Safety et le test physique final restent des gates séparés.
