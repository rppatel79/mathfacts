package com.rp.mathfacts.students.repository;

import com.rp.mathfacts.students.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {
}
