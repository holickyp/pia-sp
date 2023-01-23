package cz.zcu.kiv.pia.sp.projects.enums;

/**
 * nejmensi hodnoty datumu (casy od/do)
 * pouzite jako placeholder po vytvoreni noveho assignmentu
 * novy assignment (Draft) -> nastaven na nejmensi hodnoty
 */
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
