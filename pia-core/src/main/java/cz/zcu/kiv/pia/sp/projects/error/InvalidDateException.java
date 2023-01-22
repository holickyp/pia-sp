package cz.zcu.kiv.pia.sp.projects.error;

import java.io.Serial;

public final class InvalidDateException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 8942675318542287163L;

    public InvalidDateException() {
        super();
    }

    public InvalidDateException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public InvalidDateException(final String message) {
        super(message);
    }

    public InvalidDateException(final Throwable cause) {
        super(cause);
    }
}
