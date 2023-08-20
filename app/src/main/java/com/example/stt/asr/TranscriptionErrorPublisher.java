package com.example.stt.asr;

/** An interface for notifying the client about ASR errors. */
public interface TranscriptionErrorPublisher {
    /** Called when error happens. */
    void onError(Throwable errorCause);
}