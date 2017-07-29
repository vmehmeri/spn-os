package spn.os;

/**
 * IPv4 address is a 4-byte . separated string
 * @author Cong Chen <Cong.Chen@us.fujitsu.com>
 *
 */
public class IPAddress extends SectionedString
{
    public IPAddress(String ipString) {
        super(ipString, 4, '.');
    }
    
    public IPAddress(long ipValue) {
        super(ipValue, 4, '.');
    }

    @Override
    public int hashCode() {
        return (int) toInt();
    }
    
    @Override
    protected String printSection(byte b) {
        return String.format("%d", b & 0xFF);
    }
    
    @Override
    protected byte sectionValue(String section) {
        return Integer.valueOf(section, 10).byteValue();
    }
    
}