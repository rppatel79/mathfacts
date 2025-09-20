package com.rp.mathfacts.mathpractice.service;

import com.rp.mathfacts.students.entity.Level;
import com.rp.mathfacts.students.entity.TestType;

import java.util.Random;

public class QuestionFactory {
    private final Random rnd = new Random();

    public int base(Level level) {
        return switch (level) {
            case BEGINNER -> 5;
            case INTERMEDIATE -> 10;
            case ADVANCED -> 12;
        };
    }

    public int expected(int a, int b, TestType type) {
        return switch (type) {
            case ADDITION -> a + b;
            case SUBTRACTION -> a - b;
            case MULTIPLICATION -> a * b;
            case DIVISION -> a / b;
        };
    }

    public int[] nextPair(Level level, TestType type) {
        int cap = base(level);
        int a, b;
        switch (type) {
            case ADDITION -> {
                a = rnd.nextInt(cap) + 1;
                b = rnd.nextInt(cap) + 1;
            }
            case SUBTRACTION -> {
                a = rnd.nextInt(cap) + 1;
                b = rnd.nextInt(cap) + 1;
                if (a < b) { int t=a; a=b; b=t; } // non-negative
            }
            case MULTIPLICATION -> {
                a = rnd.nextInt(2) + 7;  // TODO REMOVE ME
                b = rnd.nextInt(cap) + 1;
                if (rnd.nextBoolean())
                {
                    var tmp = a;
                    a=b;
                    b=tmp;
                }
            }
            case DIVISION -> {
                // generate divisible pair by construction
                b = rnd.nextInt(cap) + 1;
                int q = rnd.nextInt(cap) + 1;
                a = b * q; // a / b = q, integer division safe
            }
            default -> throw new IllegalStateException("Unexpected type: "+type);
        }
        // randomize order a/b sometimes (except we keep subtraction non-negative and division with divisor as b)
        if (type == TestType.ADDITION || type == TestType.MULTIPLICATION) {
            if (rnd.nextBoolean()) { int t=a; a=b; b=t; }
        }
        return new int[]{a,b};
    }

    public String render(int a, int b, TestType type) {
        String op = switch (type) {
            case ADDITION -> "+";
            case SUBTRACTION -> "−";
            case MULTIPLICATION -> "×";
            case DIVISION -> "÷";
        };
        return a + " " + op + " " + b;
    }
}