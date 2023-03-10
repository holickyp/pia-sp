package cz.zcu.kiv.pia.sp.projects.error;

import java.io.Serial;

/**
 * Exception, uzivatel jiz existuje (stejny username)
 */
public final class UserAlreadyExistException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 5861310537366287163L;

    public UserAlreadyExistException() {
        super();
    }

    public UserAlreadyExistException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UserAlreadyExistException(final String message) {
        super(message);
    }

    public UserAlreadyExistException(final Throwable cause) {
        super(cause);
    }
}
