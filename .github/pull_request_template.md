## Qualification de la Pull Request

La CI est la source d’autorité pour tout ce qui est objectivement vérifiable par machine. Le propriétaire du dépôt ne doit pas refaire manuellement les contrôles déjà prouvés par les workflows.

### Qualification développeur automatique

Le statut **Android developer qualification** doit être vert. Il couvre automatiquement :

- intégrité du dépôt et absence de dette source interdite ;
- conformité `targetSdk` ;
- Android Lint debug + release ;
- tests unitaires avec preuve qu’au moins un test a réellement été exécuté ;
- génération APK debug ;
- génération AAB release ;
- démarrage et parcours fonctionnels instrumentés sur appareil Android émulé géré ;
- preuve que les tests d’instrumentation ont réellement été exécutés et n’ont ni échec ni erreur.

Ces contrôles ne nécessitent **aucune validation manuelle répétée** lorsque la CI est verte.

### Contrôles complémentaires automatiques

Les autres workflows du dépôt restent applicables selon leur périmètre : Repo Guard, CodeQL, contrôles de sécurité et garde de release.

### Validation humaine résiduelle

Ne cocher que lorsqu’un changement touche réellement un élément impossible à qualifier honnêtement sur émulateur :

- [ ] Comportement dépendant d’un appareil physique/OEM explicitement vérifié.
- [ ] Interaction matérielle non émulable explicitement vérifiée.
- [ ] Décision éditoriale/commerciale de publication explicitement approuvée.
- [ ] Release signée : secrets, provenance et signature gérés uniquement par le workflow de release prévu.

Si aucune de ces catégories n’est concernée, aucune validation fonctionnelle manuelle du propriétaire n’est exigée pour la qualification développeur.

### Vérité d’état

- Une CI verte signifie **qualifié développeur**, pas automatiquement « production commerciale publiée ».
- Un échec de l’émulateur est bloquant jusqu’à correction ou preuve technique qu’il s’agit d’un défaut d’infrastructure du runner.
- Aucun état « opérationnel », « terminé » ou « prêt production » ne doit être revendiqué sans la preuve correspondant à ce niveau.
