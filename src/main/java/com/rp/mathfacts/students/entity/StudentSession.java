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
    private Level level;
    private TestType testType;
    private int correctStreak=0;
    private int incorrectStreak=0;

    @Convert(converter =  QuestionConverter.class)
    private Question currentQuestion;

}
