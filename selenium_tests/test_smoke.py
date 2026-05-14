"""
Smoke tests for EduQuiz app.
These tests cover basic app functionality and navigation.
"""
import pytest
from page_objects import HomePage, LibraryPage, QuizEntryPage, SettingsPage

@pytest.mark.smoke
def test_app_loads(driver, base_url):
    """Test that the app loads successfully."""
    driver.get(base_url)
    assert driver.title or driver.current_url.startswith(base_url)

@pytest.mark.smoke
def test_navigation_to_home(driver, wait, base_url):
    """Test navigation to home page."""
    home = HomePage(driver, wait, base_url)
    home.navigate()
    assert home.is_loaded()

@pytest.mark.smoke
def test_navigation_to_library(driver, wait, base_url):
    """Test navigation to library page."""
    library = LibraryPage(driver, wait, base_url)
    library.navigate()
    assert library.is_loaded()

@pytest.mark.smoke
def test_navigation_to_quiz(driver, wait, base_url):
    """Test navigation to quiz entry page."""
    quiz = QuizEntryPage(driver, wait, base_url)
    quiz.navigate()
    assert quiz.is_loaded()

@pytest.mark.smoke
def test_home_page_title(driver, wait, base_url):
    """Test home page displays correct heading."""
    home = HomePage(driver, wait, base_url)
    home.navigate()
    heading = home.get_heading()
    assert heading  # Heading should not be empty

@pytest.mark.smoke
@pytest.mark.auth
def test_navigation_to_settings_as_authenticated_user(authenticated_driver, wait, base_url):
    """Ensure authenticated users can navigate to settings."""
    settings = SettingsPage(authenticated_driver, wait, base_url)
    settings.navigate()
    assert settings.is_loaded()
