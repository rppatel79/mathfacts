package com.rp.mathfacts.students.controller;
import static org.hamcrest.Matchers.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rp.mathfacts.students.entity.Level;
import com.rp.mathfacts.students.entity.Student;
import com.rp.mathfacts.students.entity.StudentSession;
import com.rp.mathfacts.students.entity.TestType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    static final String DEMO_TEST_ID = "demo";

    @Test
    void getTest_byId_returnsItems() throws Exception {
        mockMvc.perform(get("/api/spelling/tests/{testId}", DEMO_TEST_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.testId").value(DEMO_TEST_ID))
                .andExpect(jsonPath("$.items", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.items[0].word", not(emptyOrNullString())))
                .andExpect(jsonPath("$.items[0].sentence", not(emptyOrNullString())));    }

    @Test
    void getAllTests_containsDemo() throws Exception {
        mockMvc.perform(get("/api/spelling/tests"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // ensure list has an entry with testId = demo
                .andExpect(jsonPath("$[*].testId", hasItem(DEMO_TEST_ID)))
                // and each entry has items (non-empty for our demo)
                .andExpect(jsonPath("$[*].items", everyItem(notNullValue())));
    }

    @Test
    void listTestIds_returnsLightweightSummary() throws Exception {
        mockMvc.perform(get("/api/spelling/tests/ids"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[*].testId", hasItem(DEMO_TEST_ID)))
                .andExpect(jsonPath("$[?(@.testId=='" + DEMO_TEST_ID + "')].itemCount",
                        hasItem(greaterThanOrEqualTo(1))));
    }

    @Test
    public void testAddAndGetStudent() throws Exception {
        // Prepare testTypeToLevel map
        Map<TestType, Level> levelMap = new EnumMap<>(TestType.class);
        levelMap.put(TestType.MULTIPLICATION, Level.BEGINNER);
        levelMap.put(TestType.ADDITION, Level.INTERMEDIATE);

        // Create student
        Student student = Student.builder()
                .id(null)
                .name("John Doe")
                .testTypeToLevel(levelMap)
                .build();

        // Convert to JSON and post
        String studentJson = objectMapper.writeValueAsString(student);

        MvcResult postResult = mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson))
                .andExpect(status().isCreated())
                .andReturn();

        Student createdStudent = objectMapper.readValue(postResult.getResponse().getContentAsString(), Student.class);
        UUID studentId = createdStudent.getId();

        // GET by ID
        mockMvc.perform(get("/students/" + studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.testTypeToLevel.MULTIPLICATION").value("BEGINNER"))
                .andExpect(jsonPath("$.testTypeToLevel.ADDITION").value("INTERMEDIATE"));
    }

    @Test
    public void testListStudents() throws Exception {
        // Create a new student with one test type
        Map<TestType, Level> levelMap = new EnumMap<>(TestType.class);
        levelMap.put(TestType.DIVISION, Level.ADVANCED);

        Student student = Student.builder()
                .name("Jane Smith")
                .testTypeToLevel(levelMap)
                .build();

        String studentJson = objectMapper.writeValueAsString(student);

        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson))
                .andExpect(status().isCreated());

        // GET all students
        MvcResult result = mockMvc.perform(get("/students"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        List<Student> students = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(students).isNotEmpty();
        assertThat(students.stream().anyMatch(s -> "Jane Smith".equals(s.getName()))).isTrue();
    }

    @Test
    public void testUpdateStudent() throws Exception {
        // Create student
        Map<TestType, Level> initialMap = new EnumMap<>(TestType.class);
        initialMap.put(TestType.SUBTRACTION, Level.BEGINNER);

        Student student = Student.builder()
                .name("Original Name")
                .testTypeToLevel(initialMap)
                .build();

        String studentJson = objectMapper.writeValueAsString(student);

        // POST to create
        MvcResult postResult = mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson))
                .andExpect(status().isCreated())
                .andReturn();

        Student createdStudent = objectMapper.readValue(postResult.getResponse().getContentAsString(), Student.class);
        UUID studentId = createdStudent.getId();

        // Modify session only
        StudentSession session = StudentSession.builder()
                .correctStreak(2)
                .incorrectStreak(1)
                .testType(TestType.MULTIPLICATION)
                .level(Level.BEGINNER)
                .build();

        Student partialUpdate = new Student(); // empty object to avoid unintentionally nulling fields
        partialUpdate.setSession(session);

        String updatedJson = objectMapper.writeValueAsString(partialUpdate);

        mockMvc.perform(put("/students/" + studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.session.correctStreak").value(2))
                .andExpect(jsonPath("$.session.incorrectStreak").value(1))
                .andExpect(jsonPath("$.session.testType").value(TestType.MULTIPLICATION.name()))
                .andExpect(jsonPath("$.session.level").value(Level.BEGINNER.name()))
                // Ensure other fields are still intact (i.e., not overwritten)
                .andExpect(jsonPath("$.name").value("Original Name"))
                .andExpect(jsonPath("$.testTypeToLevel.SUBTRACTION").value("BEGINNER"))
                .andReturn();
    }


}
