package cz.cvut.fel.bp.deckservice.exception;

/**
 * Exception thrown when invalid business logic operations are done upon a deck.
 */
public class InvalidDeckOperationException extends RuntimeException {

    /**
     * Constructs a new InvalidDeckOperationException with the specified detail message.
     * @param message the detail message explaining the reason for the exception
     */
    public InvalidDeckOperationException(String message) {
        super(message);
    }
}
