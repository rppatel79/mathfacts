package com.rp.mathfacts.mathpractice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MathPracticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetQuestion() throws Exception {
        mockMvc.perform(get("/api/math/question")
                        .param("level", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.a").isNumber())
                .andExpect(jsonPath("$.b").isNumber())
                .andExpect(jsonPath("$.question").isString());
    }

    @Test
    void testCheckAnswerCorrect() throws Exception {
        int a = 3;
        int b = 4;
        int answer = 12;

        String requestBody = String.format("""
            {
              "a": %d,
              "b": %d,
              "answer": %d
            }
            """, a, b, answer);

        mockMvc.perform(post("/api/math/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.expected").value(a * b));
    }

    @Test
    void testCheckAnswerIncorrect() throws Exception {
        int a = 5;
        int b = 6;
        int answer = 31; // wrong

        String requestBody = String.format("""
            {
              "a": %d,
              "b": %d,
              "answer": %d
            }
            """, a, b, answer);

        mockMvc.perform(post("/api/math/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(false))
                .andExpect(jsonPath("$.expected").value(a * b));
    }
}
