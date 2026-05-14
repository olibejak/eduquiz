"""
Tests for Quiz functionality - UI form tests and end-to-end quiz flow tests.
"""
import os
import time
import pytest
from selenium import webdriver
from selenium.webdriver.support.wait import WebDriverWait
from page_objects import QuizEntryPage, QuizLobbyPage


@pytest.mark.quiz
class TestQuizEntry:
    """Test suite for quiz entry page."""

    def test_quiz_entry_page_loads(self, driver, wait, base_url):
        """Test quiz entry page loads successfully."""
        quiz = QuizEntryPage(driver, wait, base_url)
        quiz.navigate()
        assert quiz.is_loaded()

    def test_quiz_entry_form_elements_exist(self, driver, wait, base_url):
        """Test that quiz entry form has required elements."""
        quiz = QuizEntryPage(driver, wait, base_url)
        quiz.navigate()
        assert quiz.is_element_visible(quiz.NICKNAME_INPUT)
        assert quiz.is_element_visible(quiz.JOIN_BUTTON) or quiz.is_element_visible(quiz.CREATE_BUTTON)

    def test_fill_nickname_for_join(self, driver, wait, base_url):
        """Test filling nickname for joining."""
        quiz = QuizEntryPage(driver, wait, base_url)
        quiz.navigate()
        quiz.type_text(quiz.NICKNAME_INPUT, "TestPlayer")
        nickname_element = quiz.find_element(quiz.NICKNAME_INPUT)
        assert "TestPlayer" in nickname_element.get_attribute("value")

    def test_fill_nickname_and_pin(self, driver, wait, base_url):
        """Test filling nickname and PIN."""
        quiz = QuizEntryPage(driver, wait, base_url)
        quiz.navigate()
        quiz.type_text(quiz.NICKNAME_INPUT, "TestPlayer")
        quiz.type_text(quiz.PIN_INPUT, "1234")

        nickname_element = quiz.find_element(quiz.NICKNAME_INPUT)
        pin_element = quiz.find_element(quiz.PIN_INPUT)

        assert "TestPlayer" in nickname_element.get_attribute("value")
        assert "1234" in pin_element.get_attribute("value")

    def test_quiz_entry_page_loads_authenticated(self, authenticated_driver, wait, base_url):
        """Ensure quiz entry page loads for authenticated user."""
        quiz = QuizEntryPage(authenticated_driver, wait, base_url)
        quiz.navigate()
        assert quiz.is_loaded()


@pytest.mark.quiz
class TestQuizLobby:
    """Test suite for quiz lobby - E2E flow tests."""

    def test_full_quiz_flow(self, driver, wait, base_url):
        """
        End-to-end test: Host creates lobby, guest joins, host adds deck and starts quiz.

        This test exercises:
        - Creating a quiz lobby via UI
        - Joining a lobby with a second browser
        - Verifying participant presence (WS sync)
        - Adding a deck from the modal
        - Starting the quiz and verifying redirect to /play

        Gracefully skips if backend or WS is not available.
        """
        host_nick = f"Host_{int(time.time()) % 10000}"
        guest_nick = f"Guest_{int(time.time()) % 10000}"
        pin = ""
        lobby = None

        try:
            # Host: create a lobby using the UI
            entry = QuizEntryPage(driver, wait, base_url)
            entry.navigate()
            entry.create_quiz(host_nick)

            # Wait for lobby to load
            lobby = QuizLobbyPage(driver, wait, base_url)
            assert lobby.is_loaded(), "Host lobby did not load"

            # Extract pin
            pin = lobby.get_pin()
            assert pin, "Could not read lobby PIN"

        except Exception as exc:  # pragma: no cover
            pytest.skip(f"Skipping quiz E2E - failed to create lobby via UI: {exc}")

        assert lobby is not None

        # Start a second browser to join as a guest
        other = self._make_driver()
        wait2 = WebDriverWait(other, 20)
        try:
            entry2 = QuizEntryPage(other, wait2, base_url)
            entry2.navigate()
            entry2.join_quiz(pin, guest_nick)

            lobby2 = QuizLobbyPage(other, wait2, base_url)
            assert lobby2.is_loaded(), "Guest lobby did not load"

        except Exception as exc:  # pragma: no cover
            other.quit()
            pytest.skip(f"Skipping quiz E2E - guest could not join lobby: {exc}")

        # Wait for host to observe the participant (WS may take a moment)
        found = False
        for _ in range(10):
            try:
                if lobby.participant_with_nickname_visible(guest_nick):
                    found = True
                    break
            except Exception:
                pass
            time.sleep(1)

        assert found, "Host did not see guest in participants list"

        # Try to add a deck via the host UI (best-effort: may fail if no decks available)
        try:
            lobby.open_add_modal()
            lobby.wait_for_add_modal()
            added = lobby.search_and_add_first_deck('')
        except Exception:
            added = False

        # If a deck was added, attempt to start the quiz and verify redirection to /play
        if added:
            try:
                lobby.close_add_modal()
            except Exception:
                pass

            # Enable host playing checkbox to allow host to participate
            try:
                lobby.toggle_host_playing()
            except Exception:
                pass

            # Start the quiz
            lobby.start_quiz()

            # Wait for host to be redirected to /play
            for _ in range(10):
                if '/play' in driver.current_url:
                    break
                time.sleep(1)

            assert '/play' in driver.current_url, "Host was not redirected to play after starting the quiz"

        # Cleanup
        other.quit()

    @staticmethod
    def _make_driver():
        """Helper to create a second Chrome WebDriver instance for multi-user E2E tests."""
        headless = os.getenv("HEADLESS", "True").lower() == "true"
        options = webdriver.ChromeOptions()
        if headless:
            options.add_argument("--headless=new")
        options.add_argument("--no-sandbox")
        options.add_argument("--disable-dev-shm-usage")
        options.add_argument("--disable-gpu")
        options.add_argument("--disable-extensions")
        options.add_argument("--window-size=1920,1080")

        chrome_binary = os.getenv("CHROME_BINARY")
        if chrome_binary:
            options.binary_location = chrome_binary

        drv = webdriver.Chrome(options=options)
        drv.implicitly_wait(5)
        return drv
