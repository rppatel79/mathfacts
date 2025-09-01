package com.rp.mathfacts.spellingpractice.service.entity;

import java.util.List;

public record TtsResponse(
        String testId,
        List<AudioInfo> results
) {}