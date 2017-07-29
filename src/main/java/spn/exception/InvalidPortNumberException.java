package spn.exception;

public class InvalidPortNumberException extends IllegalArgumentException {

    public InvalidPortNumberException(final String msg) {
        super(msg);
    }
}
