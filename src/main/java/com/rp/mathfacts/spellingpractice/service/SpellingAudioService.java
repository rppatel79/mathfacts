package com.rp.mathfacts.spellingpractice.service;


import com.rp.mathfacts.config.AppProps;
import com.rp.mathfacts.spellingpractice.entity.SpellingItem;
import com.rp.mathfacts.spellingpractice.service.entity.AudioInfo;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

@Service
public class SpellingAudioService {

    private static final HexFormat HEX = HexFormat.of();

    private final TextToSpeechService tts;
    private final StorageService storage;
    private final AppProps props;

    public SpellingAudioService(TextToSpeechService tts, StorageService storage, AppProps props) {
        this.tts = tts;
        this.storage = storage;
        this.props = props;
    }

    public List<AudioInfo> ensureBatch(String testId, List<SpellingItem> items) {
        return items.stream().map(i -> ensureAndLink(testId, i)).toList();
    }

    private AudioInfo ensureAndLink(String testId, SpellingItem item) {
        String word = item.word().trim();
        String sentence = item.sentence().trim();

    // One combined SSML prompt: “Spell the word {word}, as in: {sentence}.”
        String promptSsml = """
    <speak>
      <p>
        <s>Spell the word <emphasis level="moderate">%s</emphasis>, as in:</s>
        <break time="200ms"/>
        <s>%s</s>
      </p>
    </speak>
    """.formatted(xmlEscape(word), xmlEscape(sentence));

        // Cache key based on voice + engine + full SSML text
        String promptKey = key(testId, promptSsml, "prompt");
        if (!storage.exists(promptKey)) {
            byte[] mp3 = ttsUnchecked(() -> tts.synthesizeSsml(promptSsml));
            storage.putMp3(promptKey, mp3);
        }

        // Reuse same URL for both fields to avoid changing your DTO/UI
        var url = storage.presignGet(promptKey);
        return new AudioInfo(item.word(), url.toString(), url.toString());
    }

    private String key(String testId, String text, String kind) {
        String voice = props.getPolly().getVoiceId();
        String engine = props.getPolly().getEngine();
        String hash = sha256(voice + ":" + engine + ":" + text).substring(0, 32);
        String prefix = (testId != null && !testId.isBlank()) ? testId : "_global";
        return "spelling/" + prefix + "/" + hash + "-" + kind + ".mp3";
    }

    private static String sha256(String in) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            return HEX.formatHex(md.digest(in.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @FunctionalInterface
    private interface TtsCall { byte[] run() throws Exception; }

    private static byte[] ttsUnchecked(TtsCall call) {
        try { return call.run(); } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static String xmlEscape(String s) {
        // Minimal XML escaping for SSML content
        return s
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}