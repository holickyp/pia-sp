package cz.zcu.kiv.pia.sp.projects.ui.vo;

import java.util.UUID;

public class AssignmentVO {
    private final UUID worker_id;
    private final UUID job_id;
    private final double scope;
    private final String time_from;
    private final String time_to;
    private final String note;
    private final String status;

    public AssignmentVO(UUID worker_id, UUID job_id, double scope, String time_from, String time_to, String note, String status) {
        this.worker_id = worker_id;
        this.job_id = job_id;
        this.scope = scope;
        this.time_from = time_from;
        this.time_to = time_to;
        this.note = note;
        this.status = status;
    }

    public UUID getWorker_id() {
        return worker_id;
    }

    public UUID getJob_id() {
        return job_id;
    }

    public double getScope() {
        return scope;
    }

    public String getTime_from() {
        return time_from;
    }

    public String getTime_to() {
        return time_to;
    }

    public String getNote() {
        return note;
    }

    public String getStatus() {
        return status;
    }
}
