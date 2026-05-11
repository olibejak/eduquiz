package cz.cvut.fel.bp.deckservice.model;

import lombok.Getter;

@Getter
public enum QuestionType {

    MULTIPLE_CHOICE("Multiple Choice"),
    WRITE("Write"),
    NUMERIC("Numeric"),
    MATCHING("Matching");

    private final String name;

    QuestionType(String name) {
        this.name = name;
    }

}