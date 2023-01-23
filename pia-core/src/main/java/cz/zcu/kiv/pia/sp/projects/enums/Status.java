package cz.zcu.kiv.pia.sp.projects.enums;

/**
 * mozne stavy prirazeni uzivatelu v projektech
 */
public enum Status {
    DRAFT("Draft"),
    CANCELED("Canceled"),
    ACTIVE("Active"),
    PAST("Past");

    private final String status;

    Status(String status) {
        this.status = status;
    }

    public static Status getStatusByString(String status) {
        for(Status s : Status.values()) {
            if(s.toString().equals(status)) {
                return s;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return status;
    }
}
