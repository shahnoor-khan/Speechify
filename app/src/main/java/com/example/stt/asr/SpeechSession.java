package com.example.stt.asr;


/**
 * Speech recognizers must use this interface. Note that any initialization that if *really*
 * expensive should happen in the factory, not the speech session, as the factory will setup once
 * before streaming occurs and persist across all sessions.
 */
public abstract class SpeechSession {
    private boolean initialized = false;
    private int sessionID;
    /** Returns true if internet is an initialization requirement. */
    public abstract boolean requiresNetworkConnection();

    /**
     * Any setup that requires network connection should happen here.
     *
     * <p>This must not be called multiple times.
     */
    public void init(
            SpeechRecognitionModelOptions modelOptions, int bufferSizeSamples, int sessionID) {
        if (isInitialized()) {
            throw new IllegalStateException("Do not call initialize multiple times!");
        }
        this.sessionID = sessionID;
        initImpl(modelOptions, bufferSizeSamples);
        initialized = true;
    }

    public int sessionID() {
        return sessionID;
    }

    protected abstract void initImpl(
            SpeechRecognitionModelOptions modelOptions, int bufferSizeSamples);

    /** Returns true if init has been called already. */
    public final boolean isInitialized() {
        return initialized;
    }

    /** Passes audio to the session, formatted as int16 samples. */
    public boolean processAudioBytes(byte[] buffer, int offset, int count) {
        if (!isInitialized()) {
            throw new IllegalStateException("Do not call processAudioBytes before init()!");
        }
        return processAudioBytesImpl(buffer, offset, count);
    }

    protected abstract boolean processAudioBytesImpl(byte[] buffer, int offset, int count);

    /**
     * Begin the process of ending the speech session. The session need not be fully closed by the
     * time this function returns. To signal that the session is fully closed, use
     * SpeechSessionListener.onOkToTerminate() (the listener is passed into the session's
     * constructor).
     *
     * <p>This must not cause isInitialized to return false. This may be called multiple times.
     */
    public void requestCloseSession() {
        if (!isInitialized()) {
            throw new IllegalStateException("Do not call requestCloseSession before init()!");
        }
        requestCloseSessionImpl();
    }

    protected abstract void requestCloseSessionImpl();
}

