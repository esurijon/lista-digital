# Build backend user app
docker build -t esurijon/listadigital-backend:latest -f DockerfileBackendApp ../backend || exit 1

# Build frontend app
docker build -t esurijon/listadigital-frontend:latest -f DockerfileFrontendApp ../frontend || exit 1