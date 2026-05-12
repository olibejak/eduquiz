package cz.cvut.fel.bp.userservice.exception;

/**
 * Custom exception for handling cases that would result in a duplicate resource in the database.
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
