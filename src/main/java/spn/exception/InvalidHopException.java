package spn.exception;

// TODO(basus): Add fields for source and destination, rather than just a string
public class InvalidHopException extends Exception {

    public InvalidHopException(final String msg) {
        super(msg);
    }
}
