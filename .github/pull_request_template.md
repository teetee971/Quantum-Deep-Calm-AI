## ✅ Checklist de conformité – Pull Request

Merci de valider **chaque point** avant soumission.
Toute non-conformité peut entraîner le rejet automatique de la PR par la CI.

---

### 1️⃣ Conformité technique
- [ ] Aucun fichier Git LFS ajouté
- [ ] Aucun pointeur LFS présent dans le HEAD
- [ ] Dépôt compatible Cloudflare Pages (build OK)
- [ ] Aucune dépendance externe non autorisée

---

### 2️⃣ Sécurité & garde-fous CI
- [ ] Aucun fichier interdit ajouté :
  - Design : `.psd`, `.ai`, `.fig`, `.sketch`, `.xd`
  - Vidéo : `.mp4`, `.mov`, `.avi`, `.mkv`, `.webm`
  - Archives : `.zip`, `.rar`, `.7z`
- [ ] Aucun dossier temporaire ou obsolète :
  - `backup/`, `old/`, `archive/`, `extract/`, `final/`, `draft/`, `history/`
- [ ] Aucun fichier marketing à la racine (`A_*.png`, `*mockup*`, `*screenshot*`, etc.)

---

### 3️⃣ Propreté du dépôt
- [ ] Aucun fichier inutile ou legacy conservé
- [ ] `.gitignore` respecté et non contourné
- [ ] Taille du dépôt inchangée ou optimisée

---

### 4️⃣ Architecture & documentation
- [ ] Séparation respectée :
  - Code applicatif
  - Assets CDN
  - Contenu marketing
- [ ] Documentation mise à jour si nécessaire
- [ ] Aucun impact non documenté sur l’architecture existante

---

### 5️⃣ Validation finale
- [ ] CI Repo Guard verte
- [ ] Aucune modification fonctionnelle non validée
- [ ] PR prête à être mergée

---

🛡️ **Rappel :**
Ce dépôt applique une politique **zéro tolérance** pour les fichiers non conformes.
Toute infraction bloque automatiquement la compilation et le merge.
