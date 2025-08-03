package com.rp.mathfacts.students.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum TestType {
    ADDITION('+'),
    SUBTRACTION('-'),
    MULTIPLICATION('x'),
    DIVISION('÷');

    private final char symbol;
    TestType(char symbol)
    {
        this.symbol = symbol;
    }

    @JsonCreator
    public static TestType fromString(String value) {
        return TestType.valueOf(value.toUpperCase());
    }

}
