package cz.cvut.fel.bp.quizservice.model;

public enum ParticipantRole {

    USER("ROLE_USER"),
    HOST("ROLE_HOST");

    private final String name;

    ParticipantRole(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
