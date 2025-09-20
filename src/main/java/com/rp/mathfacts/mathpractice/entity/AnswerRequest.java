package com.rp.mathfacts.mathpractice.entity;

import com.rp.mathfacts.students.entity.TestType;

public record AnswerRequest(int a, int b, int answer, TestType testType) {}
