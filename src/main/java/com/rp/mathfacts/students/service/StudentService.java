package com.rp.mathfacts.students.service;

import com.rp.mathfacts.students.entity.Student;
import com.rp.mathfacts.students.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StudentService {
    @Autowired
    private StudentRepository studentRepository;

    public Student addStudent(Student student)
    {
        return studentRepository.save(student);
    }
    public Student getStudent(UUID id)
    {
        return studentRepository.getReferenceById(id);
    }
    public void deleteStudent(UUID id)
    {
        studentRepository.deleteById(id);
    }

}
