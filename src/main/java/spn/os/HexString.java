package spn.os;

/**
 * A special type of SectionedString, each section being a hex number
 * @author Cong Chen <Cong.Chen@us.fujitsu.com>
 *
 */
public class HexString extends SectionedString
{
   
    public HexString(long value, int length, char separator) {
        super(value, length, separator);
    }
    
    public HexString(String string, int length, char separator) {
        super(string, length, separator);
    }
    
    @Override
    protected String printSection(byte b) {
        return String.format("%02X", b);
    }

    @Override
    protected byte sectionValue(String section) {
        return Integer.valueOf(section, 16).byteValue();
    }

}
