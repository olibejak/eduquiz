"""
Page Object Models for EduQuiz app.
Encapsulates page-specific locators and operations.
"""
import time
from selenium.webdriver.common.by import By
from selenium.webdriver.support.expected_conditions import presence_of_element_located, visibility_of_element_located


class BasePage:
    """Base page class with common methods."""

    def __init__(self, driver, wait, base_url):
        self.driver = driver
        self.wait = wait
        self.base_url = base_url

    def navigate_to(self, path=""):
        """Navigate to a specific path.

        Uses urljoin to correctly build the URL and validates the base_url.
        """
        from urllib.parse import urljoin

        base = (self.base_url or '').strip()
        if not base:
            raise ValueError("Base URL is empty. Set BASE_URL environment variable or provide a valid base_url fixture.")

        # Ensure base contains a scheme
        if not base.startswith('http://') and not base.startswith('https://'):
            base = 'http://' + base

        url = urljoin(base.rstrip('/') + '/', path.lstrip('/'))
        self.driver.get(url)

    def find_element(self, locator):
        """Find an element and wait for it to be visible."""
        return self.wait.until(visibility_of_element_located(locator))

    def find_elements(self, locator):
        """Find multiple elements."""
        return self.driver.find_elements(*locator)

    def click(self, locator):
        """Click an element."""
        element = self.find_element(locator)
        element.click()
        return element

    def type_text(self, locator, text):
        """Type text into an element."""
        element = self.find_element(locator)
        element.clear()
        element.send_keys(text)
        return element

    def get_text(self, locator):
        """Get text from an element."""
        element = self.find_element(locator)
        return element.text

    def is_element_visible(self, locator):
        """Check if an element is visible."""
        try:
            self.find_element(locator)
            return True
        except:
            return False

    def wait_for_element(self, locator):
        """Wait for an element to be present."""
        return self.wait.until(presence_of_element_located(locator))


# ==========================================
# Home Page
# ==========================================
class HomePage(BasePage):
    """Home page object model."""

    # Locators
    HEADING = (By.XPATH, "//h1[contains(@class, 'text-3xl')]")
    LOGIN_BUTTON = (By.XPATH, "//button[contains(text(), 'Přihlášení')]")
    QUIZ_HISTORY_TABLE = (By.XPATH, "//table")
    QUIZ_HISTORY_HEADING = (By.XPATH, "//h2[contains(text(), 'Historie kvízů')]")

    def navigate(self):
        """Navigate to home page."""
        self.navigate_to("/")

    def is_loaded(self):
        """Check if home page is loaded."""
        return self.is_element_visible(self.HEADING)

    def get_heading(self):
        """Get main heading text."""
        return self.get_text(self.HEADING)


# ==========================================
# Library Page
# ==========================================
class LibraryPage(BasePage):
    """Library page object model."""

    # Locators
    LIBRARY_HEADING = (By.XPATH, "//h1[contains(text(), 'Knihovna sad')]")
    ALL_DECKS_TAB = (By.XPATH, "//button[contains(text(), 'Všechny sady')]")
    MY_DECKS_TAB = (By.XPATH, "//button[contains(text(), 'Moje sady')]")
    FAVORITES_TAB = (By.XPATH, "//button[contains(text(), 'Oblíbené')]")
    SEARCH_INPUT = (By.XPATH, "//input[starts-with(@placeholder, 'Hledat podle názvu...')]")
    DECK_CARD = (By.XPATH, "//div[contains(@class, 'cursor-pointer') and .//h2]")
    NO_DECKS_MESSAGE = (By.XPATH, "//p[contains(text(), 'Zatím tu nejsou žádné sady')]")

    def navigate(self):
        """Navigate to library page."""
        self.navigate_to("/library")

    def is_loaded(self):
        """Check if library page is loaded."""
        return self.is_element_visible(self.LIBRARY_HEADING)

    def search_deck(self, search_term):
        """Search for a deck by name."""
        self.type_text(self.SEARCH_INPUT, search_term)

    def get_deck_count(self):
        """Get the number of visible decks."""
        return len(self.find_elements(self.DECK_CARD))

    def click_all_decks_tab(self):
        """Click the 'All Decks' tab."""
        self.click(self.ALL_DECKS_TAB)

    def click_my_decks_tab(self):
        """Click the 'My Decks' tab."""
        self.click(self.MY_DECKS_TAB)

    def click_favorites_tab(self):
        """Click the 'Favorites' tab."""
        self.click(self.FAVORITES_TAB)


# ==========================================
# Create Deck Page
# ==========================================
class CreateDeckPage(BasePage):
    """Create deck page object model."""

    # Locators
    TITLE_INPUT = (By.XPATH, "//input[@placeholder='Název sady']")
    DESCRIPTION_INPUT = (By.XPATH, "//textarea[@placeholder='Popis']")
    VISIBILITY_SELECT = (By.XPATH, "//select")
    ADD_QUESTION_BUTTON = (By.XPATH, "//button[contains(text(), 'Přidat otázku')]")
    SAVE_BUTTON = (By.XPATH, "//button[contains(text(), 'Uložit kompletní sadu')]")
    QUESTION_INPUT = (By.XPATH, "//input[@placeholder='Text otázky']")
    ADD_ANSWER_BUTTON = (By.XPATH, "//button[normalize-space()='+ Odpověď']")
    ANSWER_INPUT = (By.XPATH, "//input[@placeholder='Text odpovědi...']")
    CORRECT_CHECKBOX = (By.XPATH, "//label[contains(., 'Správně')]//input[@type='checkbox']")
    CREATE_HEADING = (By.XPATH, "//h1[contains(text(), 'Vytvořit')]")

    def navigate(self):
        """Navigate to create deck page."""
        self.navigate_to("/create")

    def is_loaded(self):
        """Check if create deck page is loaded."""
        return self.is_element_visible(self.CREATE_HEADING)

    def create_deck(self, title, description, visibility="PUBLIC"):
        """Create a deck with given details."""
        self.type_text(self.TITLE_INPUT, title)
        self.type_text(self.DESCRIPTION_INPUT, description)

    def _latest(self, locator):
        elements = self.find_elements(locator)
        if not elements:
            raise AssertionError(f"No elements found for locator: {locator}")
        return elements[-1]

    def add_question_with_answers(self, question_text, correct_answer, incorrect_answer=""):
        """Add a valid multiple-choice question with at least one correct answer."""
        self.click(self.ADD_QUESTION_BUTTON)
        self._latest(self.QUESTION_INPUT).send_keys(question_text)

        self.click(self.ADD_ANSWER_BUTTON)
        answer_inputs = self.find_elements(self.ANSWER_INPUT)
        answer_inputs[-1].send_keys(correct_answer)
        self._latest(self.CORRECT_CHECKBOX).click()

        if incorrect_answer:
            self.click(self.ADD_ANSWER_BUTTON)
            answer_inputs = self.find_elements(self.ANSWER_INPUT)
            answer_inputs[-1].send_keys(incorrect_answer)

    def create_sample_deck(self, title, description, question_text="2 + 2?", correct_answer="4", incorrect_answer="5"):
        """Populate the page with a minimal valid deck and save it."""
        self.create_deck(title, description)
        self.add_question_with_answers(question_text, correct_answer, incorrect_answer)
        self.save_deck()

    def add_question(self, question_text):
        """Add a question to the deck."""
        self.click(self.ADD_QUESTION_BUTTON)
        # Wait for question input to appear
        self.type_text(self.QUESTION_INPUT, question_text)

    def save_deck(self):
        """Save the deck."""
        self.click(self.SAVE_BUTTON)
        try:
            for _ in range(20):
                try:
                    self.driver.switch_to.alert.accept()
                    break
                except Exception:
                    time.sleep(0.1)
        except Exception:
            pass


# ==========================================
# Quiz Entry Page
# ==========================================
class QuizEntryPage(BasePage):
    """Quiz entry page object model."""

    # Locators
    PIN_INPUT = (By.XPATH, "//input[@placeholder='PIN hry']")
    NICKNAME_INPUT = (By.XPATH, "//input[@placeholder='Přezdívka']")
    CREATE_MODE_TOGGLE = (By.XPATH, "//button[contains(text(), 'Založit novou hru')]")
    JOIN_BUTTON = (By.XPATH, "//button[contains(text(), 'Vstoupit do hry')]")
    CREATE_BUTTON = (By.XPATH, "//button[contains(text(), 'Vytvořit hru')]")
    QUIZ_HEADING = (By.XPATH, "//h1[contains(text(), 'EduQuiz')]")

    def navigate(self):
        """Navigate to quiz entry page."""
        self.navigate_to("/quiz")

    def is_loaded(self):
        """Check if quiz entry page is loaded."""
        return self.is_element_visible(self.QUIZ_HEADING)

    def join_quiz(self, pin, nickname):
        """Join an existing quiz."""
        self.type_text(self.PIN_INPUT, pin)
        self.type_text(self.NICKNAME_INPUT, nickname)
        self.click(self.JOIN_BUTTON)

    def create_quiz(self, nickname):
        """Create a new quiz."""
        self.click(self.CREATE_MODE_TOGGLE)
        self.type_text(self.NICKNAME_INPUT, nickname)
        self.click(self.CREATE_BUTTON)


# ==========================================
# Quiz Lobby Page
# ==========================================
class QuizLobbyPage(BasePage):
    """Quiz lobby page object model."""

    # Locators
    LOBBY_PIN_DESKTOP = (By.XPATH, "//div[contains(@class, 'text-4xl') and contains(@class, 'font-black') and normalize-space(text()) != '']")
    LOBBY_PIN_MOBILE = (By.XPATH, "//span[contains(normalize-space(), 'PIN:')]")
    START_BUTTON = (By.XPATH, "//button[contains(text(), 'Zahájit kvíz')]")
    LEAVE_BUTTON = (By.XPATH, "//button[contains(text(), 'Odejít')]")
    PLAYERS_COUNT = (By.XPATH, "//span[contains(text(), 'Hráči')]")
    ADD_DECK_BUTTON = (By.XPATH, "//button[contains(text(), '+ Přidat sadu z knihovny')]")
    HOST_PLAYING_CHECKBOX = (By.XPATH, "//input[@type='checkbox']")
    # Modal / search locators
    ADD_MODAL_HEADING = (By.XPATH, "//h2[contains(text(), 'Přidat sadu')]")
    ADD_MODAL_CLOSE_BUTTON = (By.XPATH, "//div[contains(@class, 'fixed inset-0')]//button[contains(text(), '×')]")
    SEARCH_MODAL_INPUT = (By.XPATH, "//input[@placeholder='Hledat podle názvu sady...']")
    SEARCH_RESULT_ADD_BUTTON = (By.XPATH, "//div[contains(@class, 'fixed inset-0')]//button[normalize-space()='Přidat']")
    PARTICIPANT_NAME = (By.XPATH, "//div[contains(@class,'truncate')]//span")

    def is_loaded(self):
        """Check if lobby page is loaded."""
        return self.is_element_visible(self.LEAVE_BUTTON)

    def get_pin(self):
        """Get the lobby PIN."""
        try:
            return self.get_text(self.LOBBY_PIN_DESKTOP)
        except Exception:
            text = self.get_text(self.LOBBY_PIN_MOBILE)
            return text.replace("PIN:", "").strip()

    def start_quiz(self):
        """Start the quiz."""
        self.click(self.START_BUTTON)

    def leave_lobby(self):
        """Leave the lobby."""
        self.click(self.LEAVE_BUTTON)

    def toggle_host_playing(self):
        """Toggle 'Host is playing' checkbox."""
        self.click(self.HOST_PLAYING_CHECKBOX)

    def open_add_modal(self):
        """Open the 'Add deck' modal."""
        self.click(self.ADD_DECK_BUTTON)

    def wait_for_add_modal(self):
        """Wait for add modal to appear."""
        return self.wait_for_element(self.ADD_MODAL_HEADING)

    def close_add_modal(self):
        """Close the add deck modal if it is open."""
        try:
            self.click(self.ADD_MODAL_CLOSE_BUTTON)
        except Exception:
            pass

    def search_and_add_first_deck(self, keyword: str = ''):
        """Search for decks in the add-modal and click the first 'Přidat' button.

        Returns True if a deck was added, False otherwise.
        """
        try:
            self.type_text(self.SEARCH_MODAL_INPUT, keyword)
        except Exception:
            # Search input might not be present (tab not 'all') - ignore
            pass

        try:
            self.wait.until(lambda d: len(d.find_elements(*self.SEARCH_RESULT_ADD_BUTTON)) > 0)
            btn = self.find_elements(self.SEARCH_RESULT_ADD_BUTTON)[0]
            btn.click()
            return True
        except Exception:
            return False

    def participant_with_nickname_visible(self, nickname: str):
        """Check if a participant with the given nickname is visible in the participants list."""
        try:
            locator = (By.XPATH, f"//span[contains(normalize-space(), '{nickname}')]"
                             f" | //div[contains(@class, 'truncate') and contains(., '{nickname}')]"
                             f" | //span[contains(., '{nickname}')]")
            return len(self.find_elements(locator)) > 0
        except Exception:
            return False


# ==========================================
# Settings Page
# ==========================================
class SettingsPage(BasePage):
    """Settings page object model."""

    # Locators
    SETTINGS_HEADING = (By.XPATH, "//h1[contains(text(), 'Nastavení profilu')]")
    USERNAME_INPUT = (By.XPATH, "//label[contains(normalize-space(), 'Uživatelské jméno')]/following::input[1]")
    EMAIL_INPUT = (By.XPATH, "//label[contains(normalize-space(), 'E-mailová adresa')]/following::input[1]")
    SAVE_BUTTON = (By.XPATH, "//button[contains(text(), 'Uložit')]")
    SUCCESS_MESSAGE = (By.XPATH, "//div[contains(text(), 'úspěšně')]")
    ERROR_MESSAGE = (By.XPATH, "//div[contains(@class, 'text-red-500') and contains(@class, 'bg-red-500/10')]")

    def navigate(self):
        """Navigate to settings page."""
        self.navigate_to("/settings")

    def is_loaded(self):
        """Check if settings page is loaded."""
        return self.is_element_visible(self.SETTINGS_HEADING)

    def update_username(self, username):
        """Update username."""
        self.type_text(self.USERNAME_INPUT, username)

    def update_email(self, email):
        """Update email."""
        self.type_text(self.EMAIL_INPUT, email)

    def save_changes(self):
        """Save settings changes."""
        self.click(self.SAVE_BUTTON)

    def is_success_message_visible(self):
        """Check if success message is visible."""
        return self.is_element_visible(self.SUCCESS_MESSAGE)

    def is_error_message_visible(self):
        """Check if the error banner is visible."""
        return self.is_element_visible(self.ERROR_MESSAGE)


# ==========================================
# Flashcards Dashboard
# ==========================================
class FlashcardsDashboardPage(BasePage):
    """Flashcards dashboard page object model."""

    # Locators
    DASHBOARD_HEADING = (By.XPATH, "//h1[contains(text(), 'Studium') or contains(text(), 'Knihovna veřejných')]")
    DUE_TAB = (By.XPATH, "//button[contains(text(), 'K opakování')]")
    ALL_DECKS_TAB = (By.XPATH, "//button[contains(text(), 'Veřejné sady')]")
    DECK_BUTTON = (By.XPATH, "//button[contains(@class, 'rounded-xl')]//span")
    SEARCH_INPUT = (By.XPATH, "//input[@placeholder='Hledat sadu...']")

    def navigate(self):
        """Navigate to flashcards dashboard."""
        self.navigate_to("/flashcards")

    def is_loaded(self):
        """Check if dashboard is loaded."""
        return self.is_element_visible(self.DASHBOARD_HEADING)

    def click_due_tab(self):
        """Click the 'Due' tab."""
        self.click(self.DUE_TAB)

    def click_all_decks_tab(self):
        """Click the 'All Decks' tab."""
        self.click(self.ALL_DECKS_TAB)

    def search_deck(self, search_term):
        """Search for a deck."""
        self.type_text(self.SEARCH_INPUT, search_term)

