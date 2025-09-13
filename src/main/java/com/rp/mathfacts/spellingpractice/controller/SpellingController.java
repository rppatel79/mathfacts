package com.rp.mathfacts.spellingpractice.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rp.mathfacts.spellingpractice.entity.SpellingItem;
import com.rp.mathfacts.spellingpractice.entity.SpellingTest;
import com.rp.mathfacts.spellingpractice.service.SpellingAudioService;
import com.rp.mathfacts.spellingpractice.service.entity.TtsResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ui.Model;

@RestController
@RequestMapping("/api/spelling")
@Slf4j
public class SpellingController {

    private final ObjectMapper mapper;
    private final SpellingAudioService service;


    public SpellingController(ObjectMapper mapper, SpellingAudioService service) {
        this.mapper=mapper;
        this.service = service;
    }

    @GetMapping("/tests")
    public List<TestPayload> getAllTests() {
        return loadAllTests();
    }

    @GetMapping("/tests/ids")
    public List<TestListItem> listTestIds() {
        return loadAllTests().stream()
                .map(t -> new TestListItem(t.testId(), t.items().size()))
                .sorted(Comparator.comparing(TestListItem::testId))
                .collect(Collectors.toList());
    }


    @PostMapping("/tts")
    public TtsResponse tts(@Valid @RequestBody SpellingTest req) {
        try {
            var results = service.ensureBatch(req.testId(), req.items());
            return new TtsResponse(req.testId(), results);
        } catch (Exception e) {
            log.error("TTS failed for testId={} : {}", req.testId(), e.toString(), e);
            throw e;
        }
    }

    @GetMapping("/tests/{testId}")
    public TestPayload getTest(@PathVariable String testId) {
        var items = loadItems(testId).items();
        return new TestPayload(testId, items);
    }

    @PostMapping(value = "/check", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String check(@RequestParam String testId,
                        @RequestParam int idx,
                        @RequestParam String answer,
                        Model model) {
        List<SpellingItem> items = loadItems(testId).items();
        if (idx < 0 || idx >= items.size()) {
            model.addAttribute("ok", false);
            model.addAttribute("msg", "Invalid item.");
            return "fragments :: result";
        }
        String expected = items.get(idx).word();
        boolean ok = normalize(answer).equals(normalize(expected));
        model.addAttribute("ok", ok);
        model.addAttribute("msg", ok ? "✓ Correct" : ("✗ Incorrect — answer is “" + expected + "”"));
        return "fragments :: result";
    }

    @PostMapping(value = "/check-all", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String checkAll(@RequestParam String testId,
                           @RequestParam Map<String, String> form,
                           Model model) {
        List<SpellingItem> items = loadItems(testId).items();
        int total = items.size();
        int correct = 0;
        for (int i = 0; i < items.size(); i++) {
            String ans = form.getOrDefault("answer-" + i, "");
            if (normalize(ans).equals(normalize(items.get(i).word()))) correct++;
        }
        model.addAttribute("scoreText", "Score: " + correct + " / " + total);
        // Return an out-of-band swap to update the score area
        return "fragments :: score";
    }

    private static String normalize(String s) { return s == null ? "" : s.trim().toLowerCase(); }

    private SpellingTest loadItems(String testId) {
        try (var in = new ClassPathResource("spelling/tests/" + testId + ".json").getInputStream()) {
            return mapper.readValue(in, new com.fasterxml.jackson.core.type.TypeReference<SpellingTest>() {});
        } catch (Exception e) {
            throw new RuntimeException("Test not found or invalid: " + testId, e);
        }
    }

    private List<TestPayload> loadAllTests() {
        try {
            var resolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
            Resource[] resources = resolver.getResources("classpath*:spelling/tests/*.json");

            List<TestPayload> out = new ArrayList<>(resources.length);
            for (Resource r : resources) {
                String filename = r.getFilename();
                if (filename == null || !filename.endsWith(".json")) continue;
                String testId = filename.substring(0, filename.length() - ".json".length());
                try (InputStream in = r.getInputStream()) {
                    SpellingTest test = mapper.readValue(in, new TypeReference<SpellingTest>() {});
                    out.add(new TestPayload(testId, test.items()));
                } catch (Exception ex) {
                    log.warn("Skipping invalid test JSON {}: {}", filename, ex.toString());
                }
            }
            out.sort(Comparator.comparing(TestPayload::testId));
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load tests from classpath", e);
        }
    }

    public record TestListItem(String testId, int itemCount) {}

    public record TestPayload(String testId, List<SpellingItem> items) {}
    public record Row(int idx, String audioUrl, String sentence) {}
}