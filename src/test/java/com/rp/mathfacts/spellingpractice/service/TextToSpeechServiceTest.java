package com.rp.mathfacts.spellingpractice.service;

import com.rp.mathfacts.config.AppProps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.Engine;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.TextType;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechResponse;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TextToSpeechServiceTest {

    @Mock
    private PollyClient polly;

    // Use whichever AppProps flavor matches your implementation.
    // --- Variant A: props.getPolly().voiceId() style (from our earlier example)
    private AppProps props;

    private TextToSpeechService service;

    @BeforeEach
    void setUp() {
        // --- Variant A setup (props.getPolly().voiceId())
        props = new AppProps();

        // If your accessors are props.getPolly().voiceId() style:
        props.getPolly().setVoiceId("Joanna");
        props.getPolly().setEngine("neural");

        service = new TextToSpeechService(polly, props);

        // --- If your AppProps uses getters like getPolly().getVoiceId(), use this instead:
        // props = new AppProps();
        // var pollyCfg = new AppProps.getPolly();
        // pollyCfg.setVoiceId("Joanna");
        // pollyCfg.setEngine("neural");
        // props.setPolly(pollyCfg);
        // service = new TextToSpeechService(polly, props);
    }

    @Test
    void synthesizePlain_buildsRequestAndReturnsBytes() throws Exception {
        byte[] fakeAudio = "MP3-plain".getBytes(StandardCharsets.UTF_8);

        // Mock the streaming response: we only need readAllBytes()
        @SuppressWarnings("unchecked")
        ResponseInputStream<SynthesizeSpeechResponse> stream = mock(ResponseInputStream.class);
        when(stream.readAllBytes()).thenReturn(fakeAudio);
        when(polly.synthesizeSpeech(any(SynthesizeSpeechRequest.class))).thenReturn(stream);

        byte[] out = service.synthesizePlain("Hello world");
        assertArrayEquals(fakeAudio, out, "Should return the audio bytes from Polly");

        // Capture and assert the request built by the service
        ArgumentCaptor<SynthesizeSpeechRequest> captor = ArgumentCaptor.forClass(SynthesizeSpeechRequest.class);
        verify(polly).synthesizeSpeech(captor.capture());
        SynthesizeSpeechRequest req = captor.getValue();

        assertEquals("Joanna", req.voiceIdAsString());
        assertEquals(Engine.NEURAL, req.engine());
        assertEquals(OutputFormat.MP3, req.outputFormat());
        assertEquals(TextType.TEXT, req.textType());
        assertEquals("Hello world", req.text());
    }

    @Test
    void synthesizeSsml_buildsRequestAndReturnsBytes() throws Exception {
        byte[] fakeAudio = "MP3-ssml".getBytes(StandardCharsets.UTF_8);

        @SuppressWarnings("unchecked")
        ResponseInputStream<SynthesizeSpeechResponse> stream = mock(ResponseInputStream.class);
        when(stream.readAllBytes()).thenReturn(fakeAudio);
        when(polly.synthesizeSpeech(any(SynthesizeSpeechRequest.class))).thenReturn(stream);

        String ssml = "<speak><emphasis>Hi</emphasis></speak>";
        byte[] out = service.synthesizeSsml(ssml);
        assertArrayEquals(fakeAudio, out);

        ArgumentCaptor<SynthesizeSpeechRequest> captor = ArgumentCaptor.forClass(SynthesizeSpeechRequest.class);
        verify(polly).synthesizeSpeech(captor.capture());
        SynthesizeSpeechRequest req = captor.getValue();

        assertEquals("Joanna", req.voiceIdAsString());
        assertEquals(Engine.NEURAL, req.engine());
        assertEquals(OutputFormat.MP3, req.outputFormat());
        assertEquals(TextType.SSML, req.textType());
        assertEquals(ssml, req.text());
    }

    @Test
    void synthesizePlain_propagatesExceptions() {
        when(polly.synthesizeSpeech(any(SynthesizeSpeechRequest.class)))
                .thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> service.synthesizePlain("x"));
    }

    @Test
    void synthesizeSsml_propagatesExceptions() {
        when(polly.synthesizeSpeech(any(SynthesizeSpeechRequest.class)))
                .thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> service.synthesizeSsml("<speak>x</speak>"));
    }
}