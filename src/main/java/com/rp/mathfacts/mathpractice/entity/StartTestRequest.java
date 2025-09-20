package com.rp.mathfacts.mathpractice.entity;

import com.rp.mathfacts.students.entity.Level;
import com.rp.mathfacts.students.entity.TestType;

import java.util.UUID;

public record StartTestRequest(UUID studentId, TestType testType, Level level, Integer durationSec) {}
