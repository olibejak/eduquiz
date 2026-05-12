package cz.cvut.fel.bp.quizservice.model;

public enum SessionState {
    LOBBY("LOBBY"),
    QUIZ_STARTING("QUIZ_STARTING"),
    QUESTION_ACTIVE("QUESTION_ACTIVE"),
    QUESTION_RESULTS("QUESTION_RESULTS"),
    FINISHED("FINISHED");

    private final String name;

    SessionState(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}