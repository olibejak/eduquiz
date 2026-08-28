# EduQuiz App 🎓

> **Note:** This project was originally created and defended as a Bachelor's thesis.

🚀 **Live Demo:** [Try out EduQuiz here!](https://bp-eduquiz-app.vercel.app/)

EduQuiz is a comprehensive learning platform designed for individual study using flashcards and real-time interactive group quizzes. 

## 🏗️ Architecture Overview
- **Frontend:** React application deployed on Vercel.
- **Backend:** Java Spring Boot microservices deployed on an Oracle Cloud VM.
- **Infrastructure:** Dockerized environment using PostgreSQL, RabbitMQ, and an Nginx API Gateway.

## 📂 Structure
- [`frontend/`](frontend) - React frontend application
  - [`frontend/README.md`](frontend/README.md) - Frontend documentation
- [`backend/`](backend) - Spring Boot microservices
  - [`backend/user-service`](backend/user-service) - user service
  - [`backend/deck-service`](backend/deck-service) - deck service
  - [`backend/quiz-service`](backend/quiz-service) - quiz service
  - [`backend/flashcards-service`](backend/flashcards-service) - flashcards service
- [`nginx/`](nginx) - Nginx configuration for API gateway[cite: 3]
  - [`nginx/nginx.conf`](nginx/nginx.conf) - Nginx configuration file
- [`postgres-init/`](postgres-init) - SQL scripts to initialize PostgreSQL databases[cite: 3]
  - [`postgres-init/init.sql`](postgres-init/init.sql) - SQL initialization script[cite: 3]

- [`docker-compose.yml`](docker-compose.yml) - local infrastructure[cite: 3]
- [`build-microservices.sh`](build-microservices.sh) - script to build all Spring Boot microservices[cite: 3]
- [`.env.example`](.env.example) - environment variables template[cite: 3]
 
## ⚙️ Setup

1. Copy `.env.example` to `.env` and fill in the corresponding values[cite: 3].
2. Make sure Docker and Docker Compose are installed on your machine[cite: 3].
3. Setup the frontend ([documentation](frontend/README.md)) and test dependencies if you plan to run them locally[cite: 3].

### Run the project locally

First, build the backend services:[cite: 3]

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

