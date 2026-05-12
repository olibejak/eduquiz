#!/bin/bash

# List of your microservices
SERVICES=("quiz-service" "user-service" "deck-service" "flashcards-service")

echo "Starting build process..."

for SERVICE in "${SERVICES[@]}"; do
  echo "Processing: $SERVICE"

  # Navigate to service directory
  cd ./backend/$SERVICE || {
    echo "❌ $SERVICE directory missing"
    exit 1
  }

  # Run Maven build and tests
  if [ -f "./mvnw" ]; then
    ./mvnw clean install -DskipTests=false
  else
    mvn clean install -DskipTests=false
  fi

  # Check build status
  if [ $? -eq 0 ]; then
    echo "✅ $SERVICE"
  else
    echo "❌ $SERVICE"
    exit 1
  fi

  # Return to root
  cd ../..
done

echo "✅ All systems ready"
echo "To start the backend run: docker-compose up --build"
echo "To start the frontend run in /frontend: npm run dev"
