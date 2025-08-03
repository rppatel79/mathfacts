package com.rp.mathfacts.students.controller;

import com.rp.mathfacts.students.entity.Student;
import com.rp.mathfacts.students.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/students")
public class StudentController {
    @Autowired
    private StudentService studentService;

    @GetMapping("{id}")
    public ResponseEntity<Student> get(@PathVariable UUID id)
    {
        Student student = studentService.getStudent(id);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(student);

    }

    @PostMapping
    public ResponseEntity<Student> post(@RequestBody Student student)
    {
        Student savedStudent = studentService.addStudent(student);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedStudent);
    }

    @GetMapping
    public ResponseEntity<List<Student>> list() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @PutMapping("{id}")
    public ResponseEntity<Student> update(@PathVariable UUID id, @RequestBody Student updatedStudent) {
        Student existing = studentService.getStudent(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        // Preserve immutable fields like ID
        updatedStudent.setId(id);

        // Update in DB
        Student saved = studentService.updateStudent(updatedStudent);
        return ResponseEntity.ok(saved);
    }

}
