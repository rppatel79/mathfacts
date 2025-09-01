package com.rp.mathfacts.spellingpractice.service;

import com.rp.mathfacts.config.AppProps;
import com.rp.mathfacts.config.AwsConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("application")
@Import({AwsConfig.class, TextToSpeechService.class})
@EnabledIfEnvironmentVariable(named = "AWS_INTEGRATION", matches = "true")
class TextToSpeechServiceLiveIT {
    @Autowired
    private TextToSpeechService tts;

    @Test
    void synthesizePlain_returnsMp3Bytes() throws Exception {
        byte[] bytes = tts.synthesizePlain("Hello from integration test.");
        assertMp3(bytes);
    }

    @Test
    void synthesizeSsml_returnsMp3Bytes() throws Exception {
        String ssml = "<speak><s>Hello.</s><break time=\"200ms\"/><s>Testing Polly.</s></speak>";
        byte[] bytes = tts.synthesizeSsml(ssml);
        assertMp3(bytes);
    }

    private static void assertMp3(byte[] bytes) {
        assertNotNull(bytes, "Polly returned null bytes");
        assertTrue(bytes.length > 128, "Polly returned unexpectedly small/empty audio");

        // Basic MP3 signature check:
        // Many MP3s start with "ID3" tag, otherwise a frame may start with 0xFF (frame sync).
        boolean looksLikeMp3 =
                (bytes[0] == 'I' && bytes[1] == 'D' && bytes[2] == '3') ||
                        ((bytes[0] & 0xFF) == 0xFF);

        assertTrue(looksLikeMp3, "Audio does not look like MP3 (no ID3 tag or frame sync byte)");
    }
}
