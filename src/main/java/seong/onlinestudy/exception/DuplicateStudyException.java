package seong.onlinestudy.exception;

public class DuplicateStudyException extends RuntimeException {
    public DuplicateStudyException() {
        super();
    }

    public DuplicateStudyException(String message) {
        super(message);
    }

    public DuplicateStudyException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateStudyException(Throwable cause) {
        super(cause);
    }
}
