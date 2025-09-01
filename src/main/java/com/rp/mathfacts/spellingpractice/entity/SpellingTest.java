package com.rp.mathfacts.spellingpractice.entity;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record SpellingTest (@NotBlank String testName,
                            List<SpellingItem> items)
{
}
