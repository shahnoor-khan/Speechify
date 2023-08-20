package com.example.stt.asr;


/** Factory for creating online or offline speech sessions. */
public interface SpeechSessionFactory {
    SpeechSession create(SpeechSessionListener listener, int sampleRateHz);

    /** Cleans up any allocated resources, if there are any. */
    default void cleanup() {}
}
