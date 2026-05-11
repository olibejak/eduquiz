package cz.cvut.fel.bp.userservice.exception;

/**
 * Custom exception for handling cases in which a requested resource is not found in the database.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
