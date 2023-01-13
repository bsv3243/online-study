package seong.onlinestudy.exception;

public class InvalidAuthorizationException extends RuntimeException {
    public InvalidAuthorizationException() {
        super();
    }

    public InvalidAuthorizationException(String message) {
        super(message);
    }

    public InvalidAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidAuthorizationException(Throwable cause) {
        super(cause);
    }
}
