# Как опубликовать на GitHub

Репозиторий: `china-order-guide`. Сайт поднимется через GitHub Pages автоматически — воркфлоу уже лежит в `.github/workflows/pages.yml`.

## Способ 1. Через сайт GitHub (без git)

1. Зайди на <https://github.com/new>.
2. Repository name: `china-order-guide`.
3. Поставь **Public**.
4. Не ставь галочки README / .gitignore / license — файлы уже есть.
5. Нажми **Create repository**.
6. На странице репозитория нажми **Add file → Upload files**.
7. Выдели и перетащи **все файлы** папки `china-order-guide`, включая скрытые папки `.github` и файлы `.nojekyll`, `.gitignore`.
8. Commit changes.
9. Зайди **Settings → Pages → Build and deployment**.
10. Source: **GitHub Actions**.
11. Через минуту открой `https://<твой-логин>.github.io/china-order-guide/`.

Если скрытые файлы не загрузились через веб-интерфейс — Pages может не сработать. Тогда используй способ 2.

## Способ 2. Через git

В терминале (где установлен git):

```sh
cd china-order-guide
sh publish.sh <твой-github-логин>
```

Или вручную:

```sh
cd china-order-guide
git init
git add .
git commit -m "Initial commit: Китай без обмана"
git branch -M main
git remote add origin https://github.com/<твой-логин>/china-order-guide.git
git push -u origin main
```

При запросе пароля GitHub используй personal access token, а не пароль аккаунта.

## Проверка после публикации

- Открой `https://github.com/<логин>/china-order-guide` — должны быть `index.html`, `docs/`, `.github/workflows/pages.yml`.
- Открой вкладку **Actions** — должен пройти workflow **Deploy to GitHub Pages**.
- Открой **Settings → Pages** — там появится адрес сайта.
- Проверь, что приложение работает офлайн: открой `index.html` локально и отключи интернет.
