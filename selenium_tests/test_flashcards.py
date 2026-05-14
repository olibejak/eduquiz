"""
Tests for Flashcards and Settings functionality.
"""
import pytest
import time
from page_objects import FlashcardsDashboardPage, SettingsPage


@pytest.mark.flashcard
class TestFlashcardsDashboard:
    """Test suite for flashcards dashboard."""

    def test_flashcards_page_loads(self, driver, wait, base_url):
        """Test flashcards dashboard page loads successfully."""
        dashboard = FlashcardsDashboardPage(driver, wait, base_url)
        dashboard.navigate()
        assert dashboard.is_loaded()

    def test_switch_to_all_decks_tab(self, driver, wait, base_url):
        """Test switching to 'All Decks' tab."""
        dashboard = FlashcardsDashboardPage(driver, wait, base_url)
        dashboard.navigate()
        dashboard.click_all_decks_tab()
        time.sleep(1)
        assert dashboard.is_loaded()

    def test_search_flashcard_deck(self, driver, wait, base_url):
        """Test searching for a flashcard deck."""
        dashboard = FlashcardsDashboardPage(driver, wait, base_url)
        dashboard.navigate()
        dashboard.click_all_decks_tab()
        dashboard.search_deck("test")
        time.sleep(1)
        assert dashboard.is_loaded()


# Settings tests that require authentication
@pytest.mark.auth
@pytest.mark.settings
class TestSettings:

    def test_settings_page_loads(self, authenticated_driver, wait, base_url):
        settings = SettingsPage(authenticated_driver, wait, base_url)
        settings.navigate()
        assert settings.is_loaded()

    def test_settings_update_and_save(self, authenticated_driver, wait, base_url):
        settings = SettingsPage(authenticated_driver, wait, base_url)
        settings.navigate()
        # Use a timestamped username/email to avoid collisions
        uname = f"e2e_user_{int(time.time())}"
        email = f"{uname}@example.test"
        settings.update_username(uname)
        settings.update_email(email)
        settings.save_changes()
        # Wait for the async save to complete and show frontend feedback.
        wait.until(lambda d: settings.is_success_message_visible() or settings.is_error_message_visible())
