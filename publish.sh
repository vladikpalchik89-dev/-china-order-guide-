#!/bin/sh
# Использование: sh publish.sh <твой-github-логин>
set -e

LOGIN="$1"

if [ -z "$LOGIN" ]; then
  echo "Укажи GitHub-логин: sh publish.sh <логин>"
  exit 1
fi

REPO="china-order-guide"

git init
git add .
git commit -m "Initial commit: Китай без обмана"
git branch -M main

if git remote get-url origin >/dev/null 2>&1; then
  git remote set-url origin "https://github.com/$LOGIN/$REPO.git"
else
  git remote add origin "https://github.com/$LOGIN/$REPO.git"
fi

git push -u origin main

echo "Готово: https://github.com/$LOGIN/$REPO"
echo "Сайт: https://$LOGIN.github.io/$REPO/"
