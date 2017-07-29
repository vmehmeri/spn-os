package spn.os;

/**
 * MAC address is a 6-byte, : separated HexString
 * @author Cong Chen <Cong.Chen@us.fujitsu.com>
 */
public class MACAddress extends HexString
{
    public MACAddress(String string) {
        super(string, 6, ':');
    }
}