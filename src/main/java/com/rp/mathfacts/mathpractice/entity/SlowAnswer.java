package com.rp.mathfacts.mathpractice.entity;

import com.rp.mathfacts.students.entity.TestType;

public record SlowAnswer(int a, int b, TestType testType, boolean correct, int expected, int given, long timeMs) {}
