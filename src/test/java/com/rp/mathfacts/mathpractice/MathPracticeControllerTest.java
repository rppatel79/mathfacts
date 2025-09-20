package com.rp.mathfacts.mathpractice;

import com.rp.mathfacts.students.entity.TestType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MathTestControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // ---------- Helpers ----------
    private String startTest(String testType, String level, int durationSec) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "studentId", UUID.randomUUID(),  // optional; controller ignores it
                "testType", testType,
                "level", level,
                "durationSec", durationSec
        ));

        MvcResult res = mockMvc.perform(post("/api/math/test/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.testId").exists())
                .andExpect(jsonPath("$.durationSec").value(durationSec))
                .andReturn();

        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        assertNotNull(json.get("endsAtEpochMs"));
        return json.get("testId").asText();
    }

    private JsonNode getQuestion(String testId) throws Exception {
        MvcResult res = mockMvc.perform(get("/api/math/test/{id}/question", testId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString());
    }

    private JsonNode answer(String testId, int a, int b, int answer, String testType) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "a", a,
                "b", b,
                "answer", answer,
                "testType", testType
        ));

        MvcResult res = mockMvc.perform(post("/api/math/test/{id}/answer", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString());
    }

    // ---------- Tests ----------

    @Test
    void start_returnsId_and_times() throws Exception {
        String testId = startTest("ADDITION", "BEGINNER", 2);
        assertDoesNotThrow(() -> UUID.fromString(testId));
    }

    @Test
    void question_then_correct_answer_updates_score() throws Exception {
        String testId = startTest("ADDITION", "BEGINNER", 120);

        JsonNode q = getQuestion(testId);
        int a = q.get("a").asInt();
        int b = q.get("b").asInt();
        assertTrue(q.get("timeLeftSec").asInt() <= 120);

        int expected = a + b;
        JsonNode ans = answer(testId, a, b, expected, "ADDITION");

        assertTrue(ans.get("correct").asBoolean());
        assertEquals(expected, ans.get("expected").asInt());
        assertEquals(1, ans.get("total").asInt());
        assertEquals(1, ans.get("correctCount").asInt());
        assertEquals(0, ans.get("incorrectCount").asInt());
        assertEquals(1, ans.get("streak").asInt());
    }

    @Test
    void question_then_incorrect_answer_updates_score() throws Exception {
        String testId = startTest("MULTIPLICATION", "BEGINNER", 120);

        JsonNode q = getQuestion(testId);
        int a = q.get("a").asInt();
        int b = q.get("b").asInt();
        int correct = a * b;
        int wrong = correct + 1;

        JsonNode ans = answer(testId, a, b, wrong, "MULTIPLICATION");

        assertFalse(ans.get("correct").asBoolean());
        assertEquals(correct, ans.get("expected").asInt());
        assertEquals(1, ans.get("total").asInt());
        assertEquals(0, ans.get("correctCount").asInt());
        assertEquals(1, ans.get("incorrectCount").asInt());
        assertEquals(0, ans.get("streak").asInt());
    }

    @Test
    void subtraction_is_nonNegative() throws Exception {
        String testId = startTest("SUBTRACTION", "INTERMEDIATE", 120);
        JsonNode q = getQuestion(testId);
        int a = q.get("a").asInt();
        int b = q.get("b").asInt();
        assertTrue(a >= b, "Expected non-negative subtraction pair");
        // also answer it to ensure flow works
        JsonNode ans = answer(testId, a, b, a - b, "SUBTRACTION");
        assertTrue(ans.get("correct").asBoolean());
    }

    @Test
    void division_is_exactInteger() throws Exception {
        String testId = startTest("DIVISION", "ADVANCED", 120);
        JsonNode q = getQuestion(testId);
        int a = q.get("a").asInt();
        int b = q.get("b").asInt();
        assertNotEquals(0, b);
        assertEquals(0, a % b, "Expected exact division pair");
        JsonNode ans = answer(testId, a, b, a / b, "DIVISION");
        assertTrue(ans.get("correct").asBoolean());
    }

    @Test
    void timeExpiry_returns410_on_question_and_answer() throws Exception {
        String testId = startTest("ADDITION", "BEGINNER", 1);
        // Ensure timer expires
        sleep(1200);

        mockMvc.perform(get("/api/math/test/{id}/question", testId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isGone());

        String body = objectMapper.writeValueAsString(Map.of(
                "a", 1, "b", 1, "answer", 2, "testType", "ADDITION"
        ));
        mockMvc.perform(post("/api/math/test/{id}/answer", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isGone());
    }

    @Test
    void finish_returns_summary() throws Exception {
        String testId = startTest("MULTIPLICATION", "BEGINNER", 120);

        JsonNode q = getQuestion(testId);
        int a = q.get("a").asInt();
        int b = q.get("b").asInt();
        answer(testId, a, b, a * b, "MULTIPLICATION");

        MvcResult res = mockMvc.perform(post("/api/math/test/{id}/finish", testId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.correctCount").value(1))
                .andExpect(jsonPath("$.incorrectCount").value(0))
                .andReturn();

        JsonNode summary = objectMapper.readTree(res.getResponse().getContentAsString());
        assertTrue(summary.get("avgSecPerQ").asDouble() >= 0.0);
    }
}