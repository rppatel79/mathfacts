package com.rp.mathfacts.mathpractice.entity;

public record FinishResponse(
        int total, int correctCount, int incorrectCount, double avgSecPerQ,
        java.util.List<WrongAnswer> wrongAnswers,
        java.util.List<SlowAnswer> slowestAnswers
) {}

