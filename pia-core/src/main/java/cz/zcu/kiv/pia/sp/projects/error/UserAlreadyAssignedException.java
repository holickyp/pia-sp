package cz.zcu.kiv.pia.sp.projects.error;

import java.io.Serial;

public final class UserAlreadyAssignedException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 5268491257366287163L;

    public UserAlreadyAssignedException() {
        super();
    }

    public UserAlreadyAssignedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UserAlreadyAssignedException(final String message) {
        super(message);
    }

    public UserAlreadyAssignedException(final Throwable cause) {
        super(cause);
    }
}
