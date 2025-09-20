package com.rp.mathfacts.mathpractice;

import com.rp.mathfacts.mathpractice.entity.*;
import com.rp.mathfacts.mathpractice.service.QuestionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@RestController
@RequestMapping("/api/math/test")
public class MathPracticeController {

    private final Map<UUID, TestSession> sessions = new ConcurrentHashMap<>();
    private final QuestionFactory factory = new QuestionFactory();

    @PostMapping("/start")
    public ResponseEntity<StartTestResponse> start(@RequestBody StartTestRequest req) {
        int dur = (req.durationSec() != null && req.durationSec() > 0) ? req.durationSec() : 120; // default 2 mins
        var session = new TestSession(req.testType(), req.level(), dur);
        sessions.put(session.id, session);
        return ResponseEntity.ok(new StartTestResponse(
                session.id,
                session.endsAt.toEpochMilli(),
                session.durationSec,
                session.testType,
                session.level
        ));
    }

    @GetMapping("/{testId}/question")
    public ResponseEntity<QuestionResponse> next(@PathVariable UUID testId) {
        var s = getActive(testId);
        if (s.timeLeftSec() <= 0) return ResponseEntity.status(410).build(); // Gone (time up)
        int[] pair = factory.nextPair(s.level, s.testType);
        int a = pair[0], b = pair[1];
        String q = factory.render(a, b, s.testType);
        return ResponseEntity.ok(new QuestionResponse(a, b, q, s.timeLeftSec(), s.testType, s.level));
    }

    @PostMapping("/{testId}/answer")
    public ResponseEntity<AnswerResponse> answer(@PathVariable UUID testId, @RequestBody AnswerRequest req) {
        var s = getActive(testId);
        int timeLeft = s.timeLeftSec();
        if (timeLeft <= 0) return ResponseEntity.status(410).build();

        int expected = new QuestionFactory().expected(req.a(), req.b(), req.testType());
        boolean correct = (expected == req.answer());

        s.total++;
        if (correct) { s.correct++; s.streak++; } else { s.incorrect++; s.streak = 0; }

        long elapsed = req.elapsedMs() == null ? 0L : Math.max(0L, req.elapsedMs());
        s.logs.add(new TestSession.QLog(req.a(), req.b(), expected, req.answer(), correct, elapsed));

        return ResponseEntity.ok(new AnswerResponse(correct, expected, timeLeft, s.total, s.correct, s.incorrect, s.streak));
    }

    @PostMapping("/{testId}/finish")
    public ResponseEntity<FinishResponse> finish(@PathVariable UUID testId) {
        var s = sessions.remove(testId);
        if (s == null) return ResponseEntity.notFound().build();

        long sumMs = s.logs.stream().mapToLong(l -> l.elapsedMs).sum();
        long totalMs = sumMs > 0 ? sumMs : java.time.Duration.between(s.start, s.endsAt).toMillis();
        double avgSec = s.total == 0 ? 0.0 : (totalMs / 1000.0) / s.total;

        // All wrong answers (preserve order asked)
        var wrong = s.logs.stream()
                .filter(l -> !l.correct)
                .map(l -> new WrongAnswer(l.a, l.b, s.testType, l.expected, l.given, l.elapsedMs))
                .toList();

        // Top 5 slowest answers (by elapsedMs, descending)
        var slowest = s.logs.stream()
                .sorted(java.util.Comparator.comparingLong((TestSession.QLog l) -> l.elapsedMs).reversed())
                .limit(5)
                .map(l -> new SlowAnswer(l.a, l.b, s.testType, l.correct, l.expected, l.given, l.elapsedMs))
                .toList();

        return ResponseEntity.ok(new FinishResponse(
                s.total, s.correct, s.incorrect, avgSec, wrong, slowest
        ));
    }

    private TestSession getActive(UUID id) {
        var s = sessions.get(id);
        if (s == null) throw new NoSuchElementException("Unknown testId");
        return s;
    }
}
