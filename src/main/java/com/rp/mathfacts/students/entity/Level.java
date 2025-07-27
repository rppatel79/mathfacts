package com.rp.mathfacts.students.entity;

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


}
