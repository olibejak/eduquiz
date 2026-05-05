package cz.cvut.fel.bp.quizservice.exception;

public class InvalidSessionStateException extends RuntimeException {

    public InvalidSessionStateException(String message) {
        super(message);
    }
}

