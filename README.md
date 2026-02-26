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
* **Backend:** Java 21, Spring Boot 3, Spring Security (JWT)
* **Databáze:** PostgreSQL
* **Message Broker:** RabbitMQ (pro asynchronní události mezi mikroservisami)
* **Infrastruktura:** Docker & Docker Compose

## 📁 Struktura repozitáře (Monorepo)
```text
bp-quiz-platform/
├── frontend/               # Zdrojové kódy React aplikace
├── backend/                # Zdrojové kódy backendových služeb (Spring Boot)
│   ├── api-gateway/        # Vstupní brána a směrování požadavků
│   ├── auth-service/       # Správa uživatelů a JWT autentizace
│   ├── deck-service/       # Správa sad a otázek (CRUD)
│   └── quiz-service/       # Logika real-time kvízů (WebSockets)
├── docker-compose.yml      # Konfigurace pro spuštění celé infrastruktury
└── README.md               # Tento soubor
