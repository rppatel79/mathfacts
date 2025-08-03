package com.rp.mathfacts.students.entity;

import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSession {
    private int level;
    private TestType testType;
    private int correctStreak;
    private int incorrectStreak;

    @Convert(converter =  QuestionConverter.class)
    private Question currentQuestion;

}
