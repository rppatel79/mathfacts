package com.rp.mathfacts.spellingpractice.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rp.mathfacts.spellingpractice.entity.SpellingItem;
import com.rp.mathfacts.spellingpractice.service.SpellingAudioService;
import com.rp.mathfacts.spellingpractice.service.entity.AudioInfo;
import com.rp.mathfacts.spellingpractice.service.entity.TtsRequest;
import com.rp.mathfacts.spellingpractice.service.entity.TtsResponse;
import jakarta.validation.Valid;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    public TtsResponse tts(@Valid @RequestBody TtsRequest req) {
        var results = service.ensureBatch(req.testId(), req.test().items());
        return new TtsResponse(req.testId(), results);
    }

    @GetMapping
    public String page(@RequestParam(required = false) String testId, Model model) {
        if (!StringUtils.hasText(testId)) {
            model.addAttribute("testId", "");
            model.addAttribute("items", List.of());
            return "spelling"; // landing state
        }

        List<SpellingItem> items = loadItems(testId);
        List<AudioInfo> infos = service.ensureBatch(testId, items);

        // map word -> audio URL (you use combined prompt in promptUrl)
        Map<String, String> urlByWord = infos.stream()
                .collect(Collectors.toMap(AudioInfo::word, AudioInfo::promptUrl));

        // build view rows WITHOUT exposing the correct word
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            SpellingItem it = items.get(i);
            rows.add(new Row(i, urlByWord.getOrDefault(it.word(), ""), it.sentence()));
        }

        model.addAttribute("testId", testId);
        model.addAttribute("rows", rows);
        return "spelling";
    }

    @PostMapping(value = "/check", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String check(@RequestParam String testId,
                        @RequestParam int idx,
                        @RequestParam String answer,
                        Model model) {
        List<SpellingItem> items = loadItems(testId);
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
        List<SpellingItem> items = loadItems(testId);
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

    private List<SpellingItem> loadItems(String testId) {
        try {
            var res = new ClassPathResource("spelling/tests/" + testId + ".json");
            try (InputStream in = res.getInputStream()) {
                return mapper.readValue(in, new TypeReference<List<SpellingItem>>() {});
            }
        } catch (Exception e) {
            throw new RuntimeException("Test not found or invalid: " + testId, e);
        }
    }

    public record Row(int idx, String audioUrl, String sentence) {}
}