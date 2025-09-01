package com.rp.mathfacts.spellingpractice.entity;

import jakarta.validation.constraints.NotBlank;

public record SpellingItem(
        @NotBlank String word,
        @NotBlank String sentence
) {}
