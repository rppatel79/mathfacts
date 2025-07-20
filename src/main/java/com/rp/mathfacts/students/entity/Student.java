package com.rp.mathfacts.students.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import java.util.UUID;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    private String level;

    // Constructors
    public Student() {}


}