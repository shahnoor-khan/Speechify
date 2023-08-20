package com.example.stt;

/** An interface for sending samples to an object. */
public interface SampleProcessorInterface {
    public void init(int blockSizeSamples);
    /**
     *  Samples are PCM, 16-bit samples, formatted as a byte stream.
     */
    public void processAudioBytes(byte[] bytes, int offset, int length);

    public default void processAudioBytes(byte[] bytes) {
        processAudioBytes(bytes, 0, bytes.length);
    }

    /**
     * Call when you want the interface to stop playing. Playing may restart, so don't
     * deallocate resources here.
     */
    public void stop();
}