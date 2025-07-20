package com.rp.mathfacts.students.controller;

import com.rp.mathfacts.students.entity.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class StudentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testAddAndGetStudent() throws Exception {
        // Create a new student
        Student student = new Student(null, "John Doe", "2");

        // Convert the student to JSON
        String studentJson = objectMapper.writeValueAsString(student);

        // Perform POST request to add the student
        MvcResult postResult = mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson))
                .andExpect(status().isCreated())
                .andReturn();

        // Extract the returned student with the generated ID
        Student createdStudent = objectMapper.readValue(postResult.getResponse().getContentAsString(), Student.class);

        // Perform GET request to retrieve the student by ID
        mockMvc.perform(get("/students/" + createdStudent.getId()))
                .andExpect(status().isOk())
                .andExpect((jsonPath("$.name").value("John Doe")))
                .andExpect(jsonPath("$.level").value("2"));
    }

}
