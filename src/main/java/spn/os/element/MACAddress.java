package spn.os.element;

import java.util.Arrays;

public class MACAddress {
    public static final int LENGTH = 6;
    private byte[] address = new byte[LENGTH];


    public MACAddress(byte[] address) {
        this.address = Arrays.copyOf(address, LENGTH);
    }

    public MACAddress(String addr) {
        String[] elements = addr.split(":");
        if (elements.length != LENGTH) {
            throw new IllegalArgumentException("Incorrect format for MAC addresses");
        }

        int i = 0;
        for (String element : elements) {
            address[i] = (byte)Integer.parseInt(element, 16);
            i++;
        }

    }

}
