package com.rp.mathfacts.mathpractice.entity;

public record AnswerResponse(boolean correct, int expected, int timeLeftSec, int total, int correctCount, int incorrectCount, int streak) {}
