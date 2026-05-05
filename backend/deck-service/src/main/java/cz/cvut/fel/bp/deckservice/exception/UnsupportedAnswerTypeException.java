package cz.cvut.fel.bp.deckservice.exception;

import cz.cvut.fel.bp.deckservice.model.AnswerType;

/**
 * Exception thrown when there is no mapping strategy available for a given AnswerType.
 */
public class UnsupportedAnswerTypeException extends RuntimeException {

    /**
     * Constructs a new UnsupportedAnswerTypeException with the specified detail message.
     * @param message the detail message explaining the reason for the exception
     */
    public UnsupportedAnswerTypeException(String message) {
        super(message);
    }


    /**
     * Constructs a new UnsupportedAnswerTypeException with the specified type.
     * @param type the given invalid answer type
     */
    public UnsupportedAnswerTypeException(AnswerType type) {
        super(String.format("Unsupported answer type: %s", type));
    }
}
