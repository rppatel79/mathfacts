package com.rp.mathfacts.mathpractice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/math")
public class MathPracticeController {
    private final Random random = new Random();

    @GetMapping("/question")
    public ResponseEntity<Map<String, Object>> getQuestion(@RequestParam String level) {
        int base = switch (level) {
            case "1" -> 5;
            case "2" -> 10;
            case "3" -> 12;
            default -> 5;
        };
        int a = random.nextInt(base) + 1;
        int b = random.nextInt(base) + 1;
        return ResponseEntity.ok(Map.of(
                "a", a,
                "b", b,
                "question", a + " x " + b
        ));
    }

    @PostMapping("/answer")
    public ResponseEntity<Map<String, Object>> checkAnswer(@RequestBody Map<String, Object> payload) {
        int a = (int) payload.get("a");
        int b = (int) payload.get("b");
        int answer = (int) payload.get("answer");
        boolean correct = a * b == answer;
        return ResponseEntity.ok(Map.of(
                "correct", correct,
                "expected", a * b
        ));
    }
}
