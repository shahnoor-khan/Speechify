package com.example.stt;


import org.joda.time.Instant;

/**
 * An interface for classes that estimates labels for individual speakers.
 */
public interface SpeakerIDLabeler extends SampleProcessorInterface {
    /**
     * Tells the diarizer what time it is *now*. The expectation is that time will be incremented
     * within the calls to processAudioBytes based on the number of samples that are passed.
     */
    void setReferenceTimestamp(Instant now);

    /**
     * Asks the diarizer which speaker was most likely to be active during the time interval (start,
     * end). The same request may be made several times for the same interval, so this function should
     * be very inexpensive.
     */
    SpeakerIdInfo getSpeakerIDForTimeInterval(Instant start, Instant end);

    /**
     * Clears the labels currently stored in the diarizer. It is useful to periodically clear the
     * labels (such as at the start of every new utterance) in order to keep small the data structure
     * that holds the diarization timestamps.
     */
    void clearSpeakerIDTimestamps();

    /** Resets the state of the diarizer as if no audio has been seen. */
    void reset();
}
