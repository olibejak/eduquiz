# Vzdělávací platforma (Kvízy & Flashcards)

Tento repozitář obsahuje zdrojové kódy k bakalářské práci: **Webová kvízová aplikace pro podporu individuální i skupinové výuky**.

Cílem projektu je vytvořit hybridní výukovou platformu, která propojuje **synchronní výuku** (real-time skupinové soutěžení) a **asynchronní samostudium** (kartičky využívající Spaced Repetition).

## 🚀 Hlavní funkce
- **Skupinové kvízy (Real-time):** Interaktivní soutěžení více hráčů s nízkou latencí (využívá WebSockets).
- **Individuální výuka (Flashcards):** Samostudium pomocí kartiček s implementací algoritmu pro efektivní opakování (Spaced Repetition).
- **Správa obsahu:** Kompletní CRUD operace pro tvorbu, úpravu a sdílení sad otázek.
- **Gamifikace:** Uživatelské statistiky a sledování pokroku.

## 🛠 Použité technologie
Projekt je navržen na architektuře mikroservis a využívá kontejnerizaci pro snadné nasazení.

* **Frontend:** React, TypeScript, Tailwind CSS / Material UI (PWA)
* **Backend:** Java 21, Spring Boot 3, Spring Security (JWT) - https://start.spring.io
* **Databáze:** PostgreSQL (v18)
* **Message Broker:** RabbitMQ (pro asynchronní události mezi mikroservisami)
* **API Gateway:** Nginx (v1.29)
* **Infrastruktura:** Docker & Docker Compose

## Spuštění
export $(grep -v '^#' .env | xargs) && mvn spring-boot:run

## 📁 Struktura repozitáře (Monorepo)
```text
bp-quiz-platform/
├── frontend/               # Zdrojové kódy React aplikace
│   ├── package.json
│   └── ...
├── backend/                # Zdrojové kódy backendových služeb (Spring Boot)
│   ├── deck-service/       # Služba pro správu sad a otázek
│   ├── flashcards-service/ # Služba pro flashcards samostudium
│   ├── quiz-service/       # Služba pro real-time kvízy (WebSockets)
│   └── user-service/       # Služba pro uživatele a autentizaci
├── postgres-init/          # Inicializaci databází
│   └── init.sql            # Skript, který vytvoří DB pro každou službu
├── nginx/                  # Konfigurace pro Nginx (API Gateway)
│   └── nginx.conf          # Pravidla pro směrování (REST vs WebSockets)
├── docker-compose.yml      # Hlavní soubor pro spuštění infrastruktury
├── .gitignore              # Gitem ignorované soubory (node_modules, target, .idea atd.)
└── README.md                
