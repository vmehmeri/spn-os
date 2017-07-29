package spn.os;

import java.util.Arrays;

/**
 * A SectionedString is a string with multiple separated sections
 * Each section represents a byte
 * e.g., a MAC address 00:00:00:00:0A:01 or an ip address 192.168.1.2
 * 
 * @author Cong Chen <Cong.Chen@us.fujitsu.com>
 * 
 */
public abstract class SectionedString
{
    private final byte[] _bytes;
    private final char   _separator;
    
    /**
     * Construct the string using integer
     * @param value     the integer value
     * @param length    the number of sections
     * @param separator
     */
    public SectionedString(long value, int length, char separator)
    {
        _bytes     = new byte[length];
        _separator = separator;
        
        for(int i = 0; i < length; ++i)
            _bytes[i] = (byte) ((value >> (length-1-i)*8) & 0xFF);
    }
    
    /**
     * Construct the string using a separated string
     * @param string    the string representing the value
     * @param length    the number of sections
     * @param separator
     */
    public SectionedString(String string, int length, char separator)
    {
        _separator = separator;
        
        if (string == null)
            throw new IllegalArgumentException("Null String");
        
        // convert . to \\. because String.split() only supports regex, not wildcard
        String s = String.valueOf(_separator);
        if(s.equals("."))
            s = "\\.";
        
        String[] sections = string.split(s);
        if (sections.length != length)
            throw new IllegalArgumentException("Wrong length");

        _bytes = new byte[sections.length];
        for(int i = 0; i< sections.length; ++i)
            _bytes[i] = sectionValue(sections[i]);
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < _bytes.length; ++i)
        {
            if(i > 0)
                builder.append(_separator);
            builder.append(printSection(_bytes[i]));
        }
        return builder.toString();
    }
    
    public int toInt()
    {
        int result = 0;
        for (int i = 0; i < _bytes.length; ++i) {
            result |= ((int)_bytes[i] & 0xFF) << (_bytes.length-1-i) * 8;
        }
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if(this == obj)
            return true;
        
        if(!(obj instanceof SectionedString))
            return false;
        
        SectionedString that = (SectionedString) obj;
        return  Arrays.equals(this._bytes, that._bytes) && 
                this._separator == that._separator;
    }
    
    /**
     * Template method. Subclasses determine how to print the section
     * @param b a byte to print
     * @return the print
     */
    protected abstract String printSection(byte b);
    
    /**
     * Template method. Subclasses determine how to evaluate the section
     * @param section a section to evaluate
     * @return the byte value of the section
     */
    protected abstract byte sectionValue(String section);
    
}