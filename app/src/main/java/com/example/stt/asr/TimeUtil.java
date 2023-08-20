package com.example.stt.asr;


import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Durations;
import com.google.protobuf.util.Timestamps;
import org.joda.time.Duration;
import org.joda.time.Instant;

/**
 * Conversions between the proto and Joda timestamp representations.
 *
 * <p>Note that toInstant() drops from nanosecond to millisecond precision (which shouldn't be
 * needed for ASR applications anyhow).
 */
public final class TimeUtil {
    public static Instant toInstant(Timestamp t) {
        return new Instant(Timestamps.toMillis(t));
    }

    public static Timestamp toTimestamp(Instant t) {
        return Timestamps.fromMillis(t.getMillis());
    }

    public static Duration convert(com.google.protobuf.Duration d) {
        return Duration.millis(Durations.toMillis(d));
    }

    public static com.google.protobuf.Duration convert(Duration d) {
        return Durations.fromMillis(d.getMillis());
    }
    private TimeUtil() {}
}

