package cz.cvut.fel.bp.userservice.exception;

/**
 * Custom exception for handling invalid credentials during authentication.
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
