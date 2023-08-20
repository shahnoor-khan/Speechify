package com.example.stt.asr;

import com.example.stt.SpeakerIdInfo;
import com.example.stt.SpeakerIDLabeler;
import org.joda.time.Instant;

/** A diarizer that always reports the same speaker. */
public class AlwaysSameSpeakerIDLabeler implements SpeakerIDLabeler {
    private final SpeakerIdInfo fixedInfo;

    public AlwaysSameSpeakerIDLabeler(SpeakerIdInfo fixedInfo) {
        this.fixedInfo = fixedInfo;
    }

    @Override
    public void setReferenceTimestamp(Instant now) {}

    @Override
    public SpeakerIdInfo getSpeakerIDForTimeInterval(Instant start, Instant end) {
        return fixedInfo;
    }

    @Override
    public void init(int blockSizeSamples) {}

    @Override
    public void clearSpeakerIDTimestamps() {}

    @Override
    public void reset() {}

    @Override
    public void processAudioBytes(byte[] bytes, int offset, int length) {}

    @Override
    public void stop() {}
}

