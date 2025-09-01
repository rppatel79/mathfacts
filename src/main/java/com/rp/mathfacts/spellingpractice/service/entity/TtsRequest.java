package com.rp.mathfacts.spellingpractice.service.entity;

import com.rp.mathfacts.spellingpractice.entity.SpellingTest;
import jakarta.validation.constraints.NotNull;

public record TtsRequest(
        String testId,                 // optional, used as S3 prefix
        @NotNull SpellingTest test
        ) {}