package com.rp.mathfacts.mathpractice;

import com.rp.mathfacts.students.entity.Level;
import com.rp.mathfacts.students.entity.Question;
import com.rp.mathfacts.students.entity.TestType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/math")
public class MathPracticeController {
    private final Random random = new Random();

    @GetMapping("/question")
    public ResponseEntity<Question> getQuestion(@RequestParam Level level, @RequestParam TestType testType) {
        int base = switch (level) {
            case BEGINNER -> 5;
            case INTERMEDIATE -> 10;
            case ADVANCED -> 12;
        };
        int a = 8; // temp, just doing 8s
        int b = random.nextInt(base) + 1;

        Question question = new Question(a, b, testType, level);
        return ResponseEntity.ok(question);
    }

    @PostMapping("/answer")
    public ResponseEntity<Map<String, Object>> checkAnswer(@RequestBody Map<String, Object> payload) {
        int a = (int) payload.get("a");
        int b = (int) payload.get("b");
        int answer = (int) payload.get("answer");

        String typeStr = (String) payload.get("testType");
        TestType testType = TestType.valueOf(typeStr);

        int expected = switch (testType) {
            case ADDITION -> a + b;
            case SUBTRACTION -> a - b;
            case MULTIPLICATION -> a * b;
            case DIVISION -> a / b; // optional: handle divide by zero / rounding
        };

        boolean correct =expected == answer;
        return ResponseEntity.ok(Map.of(
                "correct", correct,
                "expected", expected
        ));
    }
}
