package com.rp.mathfacts.students.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Question {
    private int a;
    private int b;
    private TestType testType;
    private Level level;

    @JsonProperty("question")
    public String getQuestionText() {
        return a + " " + testType.getSymbol() + " " + b;
    }
}
