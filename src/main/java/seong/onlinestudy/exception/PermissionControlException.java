package seong.onlinestudy.exception;

public class PermissionControlException extends RuntimeException {
    public PermissionControlException() {
        super();
    }

    public PermissionControlException(String message) {
        super(message);
    }

    public PermissionControlException(String message, Throwable cause) {
        super(message, cause);
    }

    public PermissionControlException(Throwable cause) {
        super(cause);
    }
}
