package cz.zcu.kiv.pia.sp.projects.error;

import java.io.Serial;

public final class UserNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 4801310547362288163L;

    public UserNotFoundException() {
        super();
    }

    public UserNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UserNotFoundException(final String message) {
        super(message);
    }

    public UserNotFoundException(final Throwable cause) {
        super(cause);
    }
}
