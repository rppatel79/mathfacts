package com.rp.mathfacts.mathpractice.entity;

import com.rp.mathfacts.students.entity.Level;
import com.rp.mathfacts.students.entity.TestType;

import java.util.UUID;

public record StartTestResponse(UUID testId, long endsAtEpochMs, int durationSec, TestType testType, Level level) {}
