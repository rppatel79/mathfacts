package com.rp.mathfacts.spellingpractice.service;

import com.rp.mathfacts.config.AppProps;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.Engine;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.TextType;

@Service
public class TextToSpeechService {
    private final PollyClient polly;
    private final AppProps props;

    public TextToSpeechService(PollyClient polly, AppProps props) {
        this.polly = polly;
        this.props = props;
    }

    public byte[] synthesizePlain(String text) throws Exception{
        var req = SynthesizeSpeechRequest.builder()
                .voiceId(props.polly().voiceId())
                .engine(Engine.fromValue(props.polly().engine()))
                .outputFormat(OutputFormat.MP3)
                .text(text)
                .textType(TextType.TEXT)
                .build();
        var resp = polly.synthesizeSpeech(req);
        return resp.readAllBytes();
    }

    public byte[] synthesizeSsml(String ssml) throws Exception{
        var req = SynthesizeSpeechRequest.builder()
                .voiceId(props.polly().voiceId())
                .engine(Engine.fromValue(props.polly().engine()))
                .outputFormat(OutputFormat.MP3)
                .text(ssml)
                .textType(TextType.SSML)
                .build();
        var resp = polly.synthesizeSpeech(req);
        return resp.readAllBytes();
    }
}