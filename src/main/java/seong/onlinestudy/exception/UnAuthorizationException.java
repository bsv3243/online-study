package seong.onlinestudy.exception;

public class UnAuthorizationException extends RuntimeException {
    public UnAuthorizationException() {
        super();
    }

    public UnAuthorizationException(String message) {
        super(message);
    }

    public UnAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnAuthorizationException(Throwable cause) {
        super(cause);
    }
}
