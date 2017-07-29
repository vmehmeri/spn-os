package spn.exception;

public class InvalidChannelNumberException extends IllegalArgumentException {

    public InvalidChannelNumberException(final String msg) {
        super(msg);
    }

    public InvalidChannelNumberException(final int number) {
        super(number + " is not a valid channel number for this port.");
    }
}
