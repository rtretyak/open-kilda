package org.openkilda.pce.provider;

/**
 * {@code FlowConflictException} indicates that a conflict has occurred while processing a flow.
 */
public class FlowConflictException extends Exception {

    public FlowConflictException(String message) {
        super(message);
    }

    public FlowConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
