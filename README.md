# Quoi-regarder backend

## Swagger

access to swagger: http://localhost:8080/api/swagger-ui/index.html

## Adminer

access to adminer: http://localhost:8000

## New Version

- First, you need to checkout to the main branch

```bash
git checkout main
```

- Then, you need to pull the latest changes from the develop branch

```bash
git pull --rebase origin develop
```

- After that, you need to merge the develop branch into the main branch

```bash
git push origin +main
```

- Finally, you can run the pnpm command to release a new version

```bash
./utils/deploy.sh patch # For a patch version
./utils/deploy.sh minor # For a minor version
./utils/deploy.sh major # For a major version
```

- Now, you can rebase the develop branch
-

```bash
git checkout develop
git pull --rebase origin main
git push origin +develop
```
