"""
Selenium test suite configuration and fixtures for EduQuiz app.
"""
import os
import shutil
import pytest
import requests
from selenium import webdriver
from selenium.webdriver.support.wait import WebDriverWait
import json
from dotenv import load_dotenv


# Load environment variables from .env file
load_dotenv()


# ==========================================
# Configuration
# ==========================================
BASE_URL = os.getenv("BASE_URL", "http://localhost:5173")
IMPLICIT_WAIT = 10
EXPLICIT_WAIT = 20
IS_JWT_SECURE = os.getenv("IS_JWT_SECURE", False)

# Authentication endpoints (on backend, not frontend)
TEST_LOGIN_URL = os.getenv("TEST_LOGIN_URL", "http://localhost:8080/api/auth/test/mock-login")
TEST_DELETE_URL = os.getenv("TEST_DELETE_URL", "http://localhost:8080/api/users/me")

CHROME_OPTIONS = {
    "headless": os.getenv("HEADLESS", "True").lower() == "true",
    "disable_extensions": True,
    "disable_gpu": True,
    "no_sandbox": True,
}

# ==========================================
# Fixtures
# ==========================================
@pytest.fixture(scope="function")
def driver():
    """
    Create and teardown a Chrome WebDriver instance.
    """
    options = webdriver.ChromeOptions()

    if CHROME_OPTIONS["headless"]:
        options.add_argument("--headless")

    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")
    options.add_argument("--disable-gpu")
    options.add_argument("--disable-extensions")
    options.add_argument("--window-size=1920,1080")

    # Try to detect the installed chrome/chromium binary to help Selenium Manager
    chrome_binary = (
        os.getenv("CHROME_BINARY")
        or shutil.which("chromium")
        or shutil.which("chromium-browser")
        or shutil.which("google-chrome")
        or shutil.which("chrome")
    )
    if chrome_binary:
        options.binary_location = chrome_binary

    # Let Selenium Manager resolve and download a matching chromedriver
    driver = webdriver.Chrome(options=options)

    driver.implicitly_wait(IMPLICIT_WAIT)

    yield driver

    # Cleanup
    driver.quit()

@pytest.fixture(scope="function")
def authenticated_driver(driver, base_url):
    """
    Returns driver for authenticated user. Uses a browser-based login through the
    frontend's proxied API endpoint so the server can set the jwt_token cookie with
    same-origin origin policy. This assumes the frontend dev server proxies /api calls
    to the backend (common in React/Vue dev setups with Vite).

    On teardown it will attempt to delete the test user via the proxied endpoint.
    """
    # Construct the login URL through the frontend proxy
    # This ensures the cookie is set for the frontend origin (same-site)
    frontend_login_url = f"{base_url.rstrip('/')}/api/auth/test/mock-login"

    # Step 1: Navigate to the frontend first, then POST to the proxied login endpoint.
    # This way the browser will accept the jwt_token cookie for the frontend origin.
    try:
        driver.get(base_url)
        print(f"\n[DEBUG] Browser-based login via frontend proxy: POST {frontend_login_url}")
        login_result = driver.execute_async_script(
            """
            const url = arguments[0];
            const done = arguments[1];
            fetch(url, { method: 'POST', credentials: 'include' })
                .then(async (response) => done({ status: response.status, body: await response.text() }))
                .catch((error) => done({ error: String(error) }));
            """,
            frontend_login_url,
        )
        if isinstance(login_result, dict) and login_result.get("error"):
            pytest.skip(f"Could not perform browser-based login via frontend proxy: {login_result['error']}")
    except Exception as exc:
        pytest.skip(f"Could not perform browser-based login via frontend proxy: {exc}")

    # Step 2: Check if jwt_token cookie is set
    try:
        jwt_cookie = driver.get_cookie("jwt_token")
        if jwt_cookie:
            print(f"[DEBUG] ✓ jwt_token cookie found in browser: {jwt_cookie['value'][:50]}...")
        else:
            print("[DEBUG] ⚠ jwt_token cookie not found in browser")
    except Exception as e:
        print(f"[DEBUG] Could not read jwt_token cookie: {e}")

    # Step 3: Fetch the current user profile and seed the frontend auth state.
    user_payload = None
    try:
        if jwt_cookie:
            response = requests.get(
                TEST_DELETE_URL,
                headers={"Authorization": f"Bearer {jwt_cookie['value']}"},
                cookies={"jwt_token": jwt_cookie["value"]},
                timeout=5,
            )
            response.raise_for_status()
            user_payload = response.json()
            print(f"[DEBUG] Loaded authenticated user profile: {user_payload.get('username', '<unknown>')}")
    except Exception as exc:
        print(f"[DEBUG] Could not load user profile for localStorage seeding: {exc}")

    # Step 4: Navigate to the frontend home page and seed localStorage before app mount.
    print(f"[DEBUG] Navigating to frontend: {base_url}")
    driver.get(base_url)

    if user_payload is not None:
        try:
            driver.execute_script(
                "localStorage.setItem('user_info', arguments[0]);",
                json.dumps(user_payload),
            )
            driver.refresh()
        except Exception as exc:
            print(f"[DEBUG] Could not seed localStorage auth state: {exc}")

    current_url = driver.current_url
    print(f"[DEBUG] Browser is now at: {current_url}")

    yield driver

    # Teardown: attempt to remove the test user (best-effort cleanup).
    # Note: This uses the Python requests library, not the browser, so it makes a direct
    # call to the backend. The jwt_token cookie set in the browser won't be sent.
    # The backend may allow unauthenticated deletes for test users, or cleanup may be
    # handled separately.
    try:
        print(f"\n[DEBUG] Cleanup: DELETE {TEST_DELETE_URL}")
        response = requests.delete(TEST_DELETE_URL, timeout=5)
        print(f"[DEBUG] Delete response status: {response.status_code}")
    except Exception as exc:
        # Don't fail the test suite if cleanup fails; just log.
        print(f"Warning: failed to delete test user: {exc}")

@pytest.fixture(scope="function")
def wait(driver):
    """
    Create an explicit WebDriverWait instance.
    """
    return WebDriverWait(driver, EXPLICIT_WAIT)

@pytest.fixture(scope="function")
def base_url():
    """
    Provide the base URL to tests.
    """
    # Trim and ensure a valid scheme
    url = (BASE_URL or '').strip()
    if not url:
        url = "http://localhost:5173"
    if not url.startswith('http://') and not url.startswith('https://'):
        url = 'http://' + url
    return url

# ==========================================
# Hooks
# ==========================================
def pytest_configure(config):
    """
    Configure pytest markers.
    """
    config.addinivalue_line(
        "markers", "smoke: Mark test as a smoke test"
    )
    config.addinivalue_line(
        "markers", "auth: Mark test as an authentication test"
    )
    config.addinivalue_line(
        "markers", "deck: Mark test as a deck management test"
    )
    config.addinivalue_line(
        "markers", "quiz: Mark test as a quiz test"
    )
    config.addinivalue_line(
        "markers", "flashcard: Mark test as a flashcard test"
    )
    config.addinivalue_line(
        "markers", "settings: Mark test as a settings test"
    )
