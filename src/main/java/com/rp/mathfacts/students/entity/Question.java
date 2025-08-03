package com.rp.mathfacts.students.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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
