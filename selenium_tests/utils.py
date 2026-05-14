"""
Test utilities and fixtures for EduQuiz tests.
"""
import time
from typing import List, Tuple
from selenium.webdriver.common.by import By


class TestData:
    """Common test data."""

    # Sample user data
    TEST_USERNAME = "TestUser123"
    TEST_EMAIL = "test@example.com"
    TEST_NICKNAME = "TestPlayer"

    # Sample deck data
    TEST_DECK_TITLE = "Test Deck"
    TEST_DECK_DESCRIPTION = "This is a test deck"

    # Sample quiz data
    SAMPLE_PIN = "1234"

    # Sample flashcard data
    TEST_FLASHCARD_TERM = "Sample Term"
    TEST_FLASHCARD_DEFINITION = "Sample Definition"


class TestUtils:
    """Utility methods for tests."""

    @staticmethod
    def wait_for_text_change(element, original_text: str, timeout: int = 10) -> bool:
        """
        Wait for an element's text to change from original.

        Args:
            element: Selenium element
            original_text: Original text value
            timeout: Timeout in seconds

        Returns:
            True if text changed, False otherwise
        """
        start_time = time.time()
        while time.time() - start_time < timeout:
            if element.text != original_text:
                return True
            time.sleep(0.5)
        return False

    @staticmethod
    def wait_for_element_count(driver, locator: Tuple, expected_count: int, timeout: int = 10) -> bool:
        """
        Wait for a specific number of elements to be present.

        Args:
            driver: Selenium WebDriver
            locator: Element locator tuple (By, selector)
            expected_count: Expected number of elements
            timeout: Timeout in seconds

        Returns:
            True if count matches, False otherwise
        """
        start_time = time.time()
        while time.time() - start_time < timeout:
            elements = driver.find_elements(*locator)
            if len(elements) == expected_count:
                return True
            time.sleep(0.5)
        return False

    @staticmethod
    def scroll_to_element(driver, element):
        """Scroll to an element in the viewport."""
        driver.execute_script("arguments[0].scrollIntoView(true);", element)
        time.sleep(0.5)

    @staticmethod
    def highlight_element(driver, element, color: str = "red", duration: int = 1):
        """Highlight an element for debugging."""
        original_style = element.get_attribute('style')
        driver.execute_script(
            f"arguments[0].setAttribute('style', 'border: 3px solid {color}; {original_style}');",
            element
        )
        time.sleep(duration)
        driver.execute_script(
            f"arguments[0].setAttribute('style', '{original_style}');",
            element
        )

    @staticmethod
    def take_screenshot(driver, filename: str):
        """Take a screenshot for debugging."""
        driver.save_screenshot(filename)

    @staticmethod
    def clear_input_field(element):
        """Clear an input field the robust way."""
        element.clear()
        # For contenteditable or tricky fields
        element.send_keys("")

    @staticmethod
    def get_all_text_content(driver) -> str:
        """Get all text content from the page."""
        return driver.find_element(By.TAG_NAME, "body").text


class AssertionHelpers:
    """Custom assertion helpers for better test reporting."""

    @staticmethod
    def assert_page_title(driver, expected_title: str):
        """Assert page title matches expected value."""
        actual = driver.title
        assert actual == expected_title, f"Expected title '{expected_title}', got '{actual}'"

    @staticmethod
    def assert_url_contains(driver, expected_substring: str):
        """Assert URL contains expected substring."""
        actual = driver.current_url
        assert expected_substring in actual, f"Expected URL to contain '{expected_substring}', got '{actual}'"

    @staticmethod
    def assert_element_has_text(element, expected_text: str):
        """Assert element contains expected text."""
        actual = element.text
        assert expected_text in actual, f"Expected text to contain '{expected_text}', got '{actual}'"

    @staticmethod
    def assert_element_has_attribute(element, attribute: str, expected_value: str = None):
        """Assert element has attribute with optional value check."""
        value = element.get_attribute(attribute)
        assert value is not None, f"Element does not have attribute '{attribute}'"
        if expected_value:
            assert value == expected_value, f"Attribute '{attribute}' expected '{expected_value}', got '{value}'"


# Example usage in tests:
# from utils import TestData, TestUtils, AssertionHelpers
#
# def test_example(driver, wait, base_url):
#     # Use test data
#     username = TestData.TEST_USERNAME
#
#     # Use utilities
#     TestUtils.scroll_to_element(driver, element)
#
#     # Use assertion helpers
#     AssertionHelpers.assert_url_contains(driver, "/dashboard")

