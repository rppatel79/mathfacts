package com.rp.mathfacts.spellingpractice.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rp.mathfacts.spellingpractice.entity.SpellingItem;
import com.rp.mathfacts.spellingpractice.entity.SpellingTest;
import com.rp.mathfacts.spellingpractice.service.SpellingAudioService;
import com.rp.mathfacts.spellingpractice.service.entity.TtsResponse;
import jakarta.validation.Valid;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import org.springframework.ui.Model;

@RestController
@RequestMapping("/api/spelling")
public class SpellingController {

    private final ObjectMapper mapper;
    private final SpellingAudioService service;


    public SpellingController(ObjectMapper mapper, SpellingAudioService service) {
        this.mapper=mapper;
        this.service = service;
    }

    @PostMapping("/tts")
    public TtsResponse tts(@Valid @RequestBody SpellingTest req) {
        var results = service.ensureBatch(req.testId(), req.items());
        return new TtsResponse(req.testId(), results);
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

    public record TestPayload(String testId, List<SpellingItem> items) {}
    public record Row(int idx, String audioUrl, String sentence) {}
}