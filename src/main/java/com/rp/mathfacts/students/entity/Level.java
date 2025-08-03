package com.rp.mathfacts.students.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum Level {
    BEGINNER(1),
    INTERMEDIATE(2),
    ADVANCED(3);

    private final int level;
    Level(int level)
    {
        this.level = level;
    }

    @JsonCreator
    public static Level fromString(String value) {
        return Level.valueOf(value.toUpperCase());
    }
}
