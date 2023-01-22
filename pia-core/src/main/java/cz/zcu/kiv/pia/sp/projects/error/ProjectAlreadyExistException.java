package cz.zcu.kiv.pia.sp.projects.error;

import java.io.Serial;

public final class ProjectAlreadyExistException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1289642337366287163L;

    public ProjectAlreadyExistException() {
        super();
    }

    public ProjectAlreadyExistException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ProjectAlreadyExistException(final String message) {
        super(message);
    }

    public ProjectAlreadyExistException(final Throwable cause) {
        super(cause);
    }
}
