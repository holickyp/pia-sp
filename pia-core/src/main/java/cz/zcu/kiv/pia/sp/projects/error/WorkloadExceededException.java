package cz.zcu.kiv.pia.sp.projects.error;

import java.io.Serial;

public final class WorkloadExceededException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 2753148947362288163L;

    public WorkloadExceededException() {
        super();
    }

    public WorkloadExceededException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public WorkloadExceededException(final String message) {
        super(message);
    }

    public WorkloadExceededException(final Throwable cause) {
        super(cause);
    }
}
