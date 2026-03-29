package org.slave.mcprd.exceptions;

public final class JavaNotFoundException extends RuntimeException {

    public JavaNotFoundException() {
        super();
    }

    public JavaNotFoundException(final String message) {
        super(message);
    }

    public JavaNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public JavaNotFoundException(final Throwable cause) {
        super(cause);
    }

}
