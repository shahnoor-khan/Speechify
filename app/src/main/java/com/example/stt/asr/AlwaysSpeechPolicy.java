package com.example.stt.asr;

/** A speech detector that always reports hearing speech. */
public class AlwaysSpeechPolicy implements SpeechDetectionPolicy {
    public AlwaysSpeechPolicy() {}

    @Override
    public boolean shouldPassAudioToRecognizer() {
        return true;
    }

    @Override
    public void init(int blockSizeSamples) {}

    @Override
    public void reset() {}

    @Override
    public void processAudioBytes(byte[] bytes, int offset, int length) {}

    @Override
    public void stop() {}
}