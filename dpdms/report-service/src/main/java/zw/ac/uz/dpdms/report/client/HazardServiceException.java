package zw.ac.uz.dpdms.report.client;

/**
 * A hazard service answered, but refused or failed. forbidden = true when
 * the hazard service itself denied access (HTTP 403) - that is the hazard
 * service enforcing its own scoping rule, as the brief requires.
 */
public class HazardServiceException extends RuntimeException {

    private final boolean forbidden;

    public HazardServiceException(String message, boolean forbidden) {
        super(message);
        this.forbidden = forbidden;
    }

    public boolean isForbidden() {
        return forbidden;
    }
}
