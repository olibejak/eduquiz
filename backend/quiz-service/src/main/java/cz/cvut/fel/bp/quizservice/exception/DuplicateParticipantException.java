package cz.cvut.fel.bp.quizservice.exception;

public class DuplicateParticipantException extends RuntimeException {

    public DuplicateParticipantException(String message) {
        super(message);
    }
}

