# BP EduQuiz App

Quiz and flashcards platform for individual study and live group quizzes.

## Structure
- [`frontend/`](frontend) - React frontend application
  - [`frontend/README.md`](frontend/README.md) - Frontend documentation
- [`backend/`](backend) - Spring Boot microservices
  - [`backend/user-service`](backend/user-service) - user service
  - [`backend/deck-service`](backend/deck-service) - deck service
  - [`backend/quiz-service`](backend/quiz-service) - quiz service
  - [`backend/flashcards-service`](backend/flashcards-service) - flashcards service
- [`nginx/`](nginx) - Nginx configuration for API gateway and frontend hosting
  - [`nginx/default.conf`](nginx/nginx.conf) - Nginx configuration file
- [`postgres-init/`](postgres-init) - SQL scripts to initialize PostgreSQL databases
  - [`postgres-init/init.sql`](postgres-init/init.sql) - SQL initialization script

- [`docker-compose.yml`](docker-compose.yml) - local infrastructure
- [`build-microservices.sh`](build-microservices.sh) - script to build all Spring Boot microservices
- [`.env.example`](.env.example) - environment variables template
 
## Setup

1. Copy `.env.example` to `.env` and fill in the values.
2. Make sure Docker and Docker Compose are installed.
3. Setup frontend ([documentation](frontend/README.md)) and test dependencies if you plan to run them locally.

## Run the project

Build the backend services:

```bash
bash build-microservices.sh
```

Start the full stack:

```bash
docker-compose up --build
```

Start the frontend locally:

```bash
cd frontend
npm install
npm run dev
```

## Tests

- [`selenium_tests/`](selenium_tests) - Selenium end-to-end tests
  - [`selenium_tests/README.md`](selenium_tests/README.md) - Selenium tests documentation
- [`postman/`](postman) - Postman API tests
  - [`postman/collections/API`](postman/collections/API) - API collection
  - [`postman/environments/`](postman/environments) - Postman environments

