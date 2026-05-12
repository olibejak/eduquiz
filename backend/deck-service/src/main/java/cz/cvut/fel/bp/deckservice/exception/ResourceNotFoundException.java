package cz.cvut.fel.bp.deckservice.exception;

/**
 * Exception thrown when a requested resource (e.g., Deck, Card, Answer) is not found in the database.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     * @param message the detail message explaining the reason for the exception
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new ResourceNotFoundException with a formatted message based on the resource name and its ID.
     * @param resourceName the name of the resource (e.g., "Deck", "Card", "Answer")
     * @param id the ID of the resource that was not found
     */
    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s with ID '%s' not found.", resourceName, id));
    }
}
