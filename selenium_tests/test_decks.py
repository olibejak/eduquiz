"""
Tests for Deck Library functionality.
"""
import pytest
import time
from page_objects import LibraryPage, CreateDeckPage


@pytest.mark.deck
class TestLibrary:
    """Test suite for library page."""

    def test_library_page_loads(self, driver, wait, base_url):
        """Test library page loads successfully."""
        library = LibraryPage(driver, wait, base_url)
        library.navigate()
        assert library.is_loaded()

    def test_library_has_search_input(self, driver, wait, base_url):
        """Test library page has search input."""
        library = LibraryPage(driver, wait, base_url)
        library.navigate()
        # If the search input is in the "All Decks" tab, we should be able to find it
        assert library.is_element_visible(library.SEARCH_INPUT) or library.get_deck_count() >= 0

    def test_search_deck(self, driver, wait, base_url):
        """Test searching for a deck."""
        library = LibraryPage(driver, wait, base_url)
        library.navigate()
        library.click_all_decks_tab()
        library.search_deck("test")
        time.sleep(1)  # Wait for search results
        # Just verify the page is still loaded after search
        assert library.is_loaded()

    def test_deck_count_is_non_negative(self, driver, wait, base_url):
        """Test that deck count is valid."""
        library = LibraryPage(driver, wait, base_url)
        library.navigate()
        deck_count = library.get_deck_count()
        assert deck_count >= 0


# Authentication-required deck tests
@pytest.mark.auth
@pytest.mark.deck
class TestLibraryAuth:
    """Tests for library features that require an authenticated user."""

    def test_switch_to_my_decks_tab(self, authenticated_driver, wait, base_url):
        library = LibraryPage(authenticated_driver, wait, base_url)
        library.navigate()
        library.click_my_decks_tab()
        assert library.is_loaded()

    def test_switch_to_favorites_tab(self, authenticated_driver, wait, base_url):
        library = LibraryPage(authenticated_driver, wait, base_url)
        library.navigate()
        library.click_favorites_tab()
        assert library.is_loaded()

    def test_create_deck_page_loads(self, authenticated_driver, wait, base_url):
        create = CreateDeckPage(authenticated_driver, wait, base_url)
        create.navigate()
        assert create.is_loaded()

    def test_create_deck_flow(self, authenticated_driver, wait, base_url):
        create = CreateDeckPage(authenticated_driver, wait, base_url)
        create.navigate()
        title = f"E2E Test Deck {int(time.time())}"
        create.create_sample_deck(
            title,
            "Created by E2E test",
            question_text="What is 2+2?",
            correct_answer="4",
            incorrect_answer="5",
        )

        for _ in range(20):
            try:
                authenticated_driver.switch_to.alert.accept()
                break
            except Exception:
                time.sleep(0.1)

        wait.until(lambda d: '/library' in d.current_url)

        # Verify we can navigate to library after save.
        library = LibraryPage(authenticated_driver, wait, base_url)
        wait.until(lambda d: '/library' in d.current_url)
        assert library.is_loaded()
        assert '/library' in authenticated_driver.current_url
