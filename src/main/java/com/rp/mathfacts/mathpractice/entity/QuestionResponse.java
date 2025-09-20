package com.rp.mathfacts.mathpractice.entity;

import com.rp.mathfacts.students.entity.Level;
import com.rp.mathfacts.students.entity.TestType;

public record QuestionResponse(int a, int b, String question, int timeLeftSec, TestType testType, Level level) {}
