package com.example.stt.asr;


import android.text.Spanned;

/** An interface for notifying the most recent transcription comes from the recognizer. */
public interface TranscriptionResultUpdatePublisher {
    /** A notification about the nature of the update. */
    enum UpdateType {
        TRANSCRIPT_UPDATED,
        TRANSCRIPT_FINALIZED,
        TRANSCRIPT_CLEARED,
    }

    /** Enum defining kinds of transcript result the listeners expect to handle. */
    enum ResultSource {
        /** Provides the most recent transcript result in current session. */
        MOST_RECENT_SEGMENT,
        /** Provides the whole transcript result in current session. */
        WHOLE_RESULT
    }

    /**
     * Called when transcription updates from the server.
     *
     * @param formattedResult The formatted result for the transcription.
     * @param updateType The nature of the update.
     */
    void onTranscriptionUpdate(Spanned formattedResult, UpdateType updateType);
}

