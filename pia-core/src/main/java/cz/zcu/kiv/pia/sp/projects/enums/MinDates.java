package cz.zcu.kiv.pia.sp.projects.enums;

public enum MinDates {
    MIN_DATE("0999-12-27"),
    DEFAULT_DATE("1000-01-01");

    private final String minDate;

    MinDates(String minDate) {
        this.minDate = minDate;
    }

    @Override
    public String toString() {
        return minDate;
    }
}
