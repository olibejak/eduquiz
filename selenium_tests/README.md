# Selenium Tests

Selenium e2e tests for the frontend.

## What it tests

- app loads and basic navigation
- deck pages and create deck flow
- quiz pages
- flashcards pages
- authenticated pages like favorites and my decks

## Setup

1. Start the frontend and backend.
2. Run the setup script:

```bash
bash setup.sh
```

3. Edit the created `.env` file if needed:

```bash
BASE_URL=http://localhost:5173
HEADLESS=True
TEST_LOGIN_URL=http://localhost:8080/api/auth/test/mock-login
TEST_DELETE_URL=http://localhost:8080/api/users/me
```

4. Or install dependencies, create `.env` (use `.env.example`) and run tests manually:

```bash
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
pytest -v
```

## Notes

- Run in headless mode by default.
- Use `HEADLESS=False pytest -v` to see the browser.

