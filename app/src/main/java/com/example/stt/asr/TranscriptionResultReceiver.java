package com.example.stt.asr;


import com.example.stt.asr.RequestForRecognitionThread.Action;
import com.google.common.base.Objects;
import com.google.common.flogger.FluentLogger;
import java.lang.ref.WeakReference;

/**
 * Handles results as they come in from the recognition module and posts them back to the
 * RepeatingRecognitionSession.
 */
class TranscriptionResultReceiver implements SpeechSessionListener {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private final WeakReference<RepeatingRecognitionSession.PostHandler> postHandlerRef;

    public TranscriptionResultReceiver(RepeatingRecognitionSession.PostHandler postHandler) {
        this.postHandlerRef = new WeakReference<>(postHandler);
    }

    @Override
    public void onSessionFatalError(int sessionID, Throwable error) {
        logger.atSevere().withCause(error).log("Session #%d ended fatally.", sessionID);
        post(
                RequestForRecognitionThread.newBuilder()
                        .setAction(
                                errorIndicatesLossOfConnection(error)
                                        ? Action.HANDLE_NETWORK_CONNECTION_FATAL_ERROR
                                        : Action.HANDLE_NON_NETWORK_CONNECTION_FATAL_ERROR)
                        .setSessionID(sessionID)
                        .setErrorCause(error)
                        .build());
    }

    @Override
    public void onResults(int sessionID, TranscriptionResult result, boolean resultsAreFinal) {
        post(
                RequestForRecognitionThread.newBuilder()
                        .setSessionID(sessionID)
                        .setAction(Action.POST_RESULTS)
                        .setResult(result, resultsAreFinal)
                        .build());
    }

    @Override
    public void onDoneListening(int sessionID) {
        logger.atInfo().log("Session #%d scheduled to be ended gracefully.", sessionID);
        post(sessionID, Action.REQUEST_TO_END_SESSION);
    }

    @Override
    public void onOkToTerminateSession(int sessionID) {
        logger.atInfo().log("Session #%d scheduled to be terminated.", sessionID);
        post(sessionID, Action.OK_TO_TERMINATE_SESSION);
    }

    private boolean errorIndicatesLossOfConnection(Throwable error) {
        boolean isGrpcError = error instanceof io.grpc.StatusRuntimeException;
        if (isGrpcError) {
            return Objects.equal(io.grpc.Status.fromThrowable(error), io.grpc.Status.UNAVAILABLE);
        }
        return false;
    }

    private void post(int sessionID, RequestForRecognitionThread.Action request) {
        post(
                RequestForRecognitionThread.newBuilder()
                        .setAction(request)
                        .setSessionID(sessionID)
                        .build());
    }

    private void post(RequestForRecognitionThread request) {
        RepeatingRecognitionSession.PostHandler postHandler = postHandlerRef.get();
        if (postHandler == null) {
            return;
        }
        postHandler.post(request);
    }
}
