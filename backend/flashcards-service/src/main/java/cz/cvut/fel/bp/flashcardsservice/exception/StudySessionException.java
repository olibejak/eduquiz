package cz.cvut.fel.bp.flashcardsservice.exception;

/**
 * Exception thrown when errors occur during study session generation or management.
 */
public class StudySessionException extends FlashcardException {

    public StudySessionException(String message) {
        super(message);
    }

    public StudySessionException(String message, Throwable cause) {
        super(message, cause);
    }
}

