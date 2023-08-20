package com.example.stt.asr;

import com.example.stt.SampleProcessorInterface;

/**
 * Decides whether audio should be passed to the recognizer. Unlike the SpeechDetector, this
 * is not trying to make a fine-grain estimate about whether there is speech or not, but instead
 * it decides how to manage sessions, possibly based on the output of a SpeechDetector.
 */
public interface SpeechDetectionPolicy extends SampleProcessorInterface {
    boolean shouldPassAudioToRecognizer();

    void reset();

    /**
     * Tells the detector that there is currently evidence of speech coming from a source that is
     * external to this class (for example, getting transcription results from an ASR engine).
     *
     * <p>Use of this function is certainly not required (implementations may ignore these cues by not
     * overriding this function), but it can be used to build speech detectors that consume less power
     * when there is external evidence of speech.
     */
    default void cueEvidenceOfSpeech() {}
}