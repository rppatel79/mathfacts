package com.rp.mathfacts.mathpractice.entity;

import com.rp.mathfacts.students.entity.TestType;

public record WrongAnswer(int a, int b, TestType testType, int expected, int given, long timeMs) {}
