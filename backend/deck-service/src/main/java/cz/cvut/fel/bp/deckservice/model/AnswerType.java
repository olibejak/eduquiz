package cz.cvut.fel.bp.deckservice.model;

import lombok.Getter;

@Getter
public enum AnswerType {

    STANDARD("STANDARD"),
    MATCHING("MATCHING"),
    CHOICE("CHOICE");

    private final String name;

    AnswerType(String name) {
        this.name = name;
    }

}
