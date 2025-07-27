package com.rp.mathfacts.students.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    @Convert(converter = TestLevelMapConverter.class)
    private Map<TestType, Level> testTypeToLevel = new HashMap<>();
}