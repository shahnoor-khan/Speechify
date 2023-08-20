package com.example.stt.asr;


/** An interface for communicating recognition events to the RepeatingRecognitionSession. */
public interface SpeechSessionListener {
    /**
     * Tells the client that the recognizer has had an error from which we cannot recover. It is safe
     * to terminate the session.
     */
    void onSessionFatalError(int sessionID, Throwable error);

    /**
     * Notifies that a new transcription result is available. If resultIsFinal is false, the results
     * are subject to change.
     */
    void onResults(int sessionID, TranscriptionResult result, boolean resultIsFinal);

    /** Signals that no more audio should be sent to the recognizer. */
    void onDoneListening(int sessionID);

    /**
     * Notifies that it is safe to kill the session. Called when the recognizer is done returning
     * results.
     */
    void onOkToTerminateSession(int sessionID);
}
