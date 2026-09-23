package zw.ac.uz.dpdms.common;

/**
 * Throw this anywhere a scoping/RBAC check fails. Each hazard service's
 * controller has an @ExceptionHandler that maps this to HTTP 403.
 */
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
