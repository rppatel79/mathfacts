package com.rp.mathfacts.students.entity;

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

}
