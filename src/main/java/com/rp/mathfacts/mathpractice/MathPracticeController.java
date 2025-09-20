package com.rp.mathfacts.mathpractice;

import com.rp.mathfacts.mathpractice.entity.*;
import com.rp.mathfacts.mathpractice.service.QuestionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
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

        // Optional: if you measure per-question time on the client, post it as well; here we just approximate 0.
        s.logs.add(new TestSession.QLog(req.a(), req.b(), expected, correct, 0));

        return ResponseEntity.ok(new AnswerResponse(correct, expected, timeLeft, s.total, s.correct, s.incorrect, s.streak));
    }

    @PostMapping("/{testId}/finish")
    public ResponseEntity<FinishResponse> finish(@PathVariable UUID testId) {
        var s = sessions.remove(testId);
        if (s == null) return ResponseEntity.notFound().build();

        long totalMs = Duration.between(s.start, s.endsAt).toMillis();
        // If ended early, estimate actual run time from logs if you tracked elapsedMs; otherwise use scheduled duration - remaining.
        if (!s.logs.isEmpty()) {
            long sum = s.logs.stream().mapToLong(l -> l.elapsedMs).sum();
            totalMs = Math.max(sum, totalMs); // pick something sensible
        }
        double avgSec = s.total == 0 ? 0.0 : (totalMs / 1000.0) / s.total;
        return ResponseEntity.ok(new FinishResponse(s.total, s.correct, s.incorrect, avgSec));
    }

    private TestSession getActive(UUID id) {
        var s = sessions.get(id);
        if (s == null) throw new NoSuchElementException("Unknown testId");
        return s;
    }
}
