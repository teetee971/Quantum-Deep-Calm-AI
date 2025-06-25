
@echo off
cd /d C:\Quantum-Deep-Calm-AI

echo --- Vérification de la présence de 404.html ---
if not exist 404.html (
    echo ERREUR : 404.html introuvable dans le dossier C:\Quantum-Deep-Calm-AI
    pause
    exit /b
)

echo --- Ajout des fichiers ---
git add 404.html redirect.html vercel.json

echo --- Commit des modifications ---
git commit -m "Ajout réel de 404.html à la racine pour GitHub Pages"

echo --- Push vers la branche principal ---
git push origin principal

echo --- Terminé. ---
pause
