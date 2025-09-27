package com.rp.mathfacts.mathpractice.entity;

import com.rp.mathfacts.students.entity.Level;
import com.rp.mathfacts.students.entity.TestType;
import lombok.Getter;
import lombok.Setter;

import java.time.*;
import java.util.*;

@Getter
public class TestSession {
    private final UUID id = UUID.randomUUID();
    private final TestType testType;
    private final Level level;
    private final Instant start;
    private final Instant endsAt;
    private final int durationSec;

    @Setter
    private int total = 0, correct = 0, incorrect = 0, streak = 0;

    public static class QLog {
        public final int a, b;
        public final int expected;
        public final int given;
        public final boolean correct;
        public final long elapsedMs;

        public QLog(int a, int b, int expected, int given, boolean correct, long elapsedMs) {
            this.a = a; this.b = b;
            this.expected = expected; this.given = given;
            this.correct = correct; this.elapsedMs = elapsedMs;
        }
    }
    public final List<QLog> logs = new ArrayList<>();

    public TestSession(TestType testType, Level level, int durationSec) {
        this.testType = testType;
        this.level = level;
        this.durationSec = durationSec;
        this.start = Instant.now();
        this.endsAt = this.start.plusSeconds(durationSec);
    }

    public int timeLeftSec() {
        long left = Duration.between(Instant.now(), endsAt).toSeconds();
        return (int)Math.max(0, left);
    }

    public boolean active() { return Instant.now().isBefore(endsAt); }
}