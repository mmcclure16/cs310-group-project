package util;


/**
 * A "wrapper class" designed to represent bytes as unsigned (0-255) 
 */
public class UnsignedByteHandler {
    
    private byte b;
    private short u_b;
    
    public UnsignedByteHandler(byte b) {
        this.b = b;
        
        u_b = getAsShort(b);
    }
    
    
    /* Static Methods */
    
    public static short getAsShort(byte b) {
        return (short)(b & 0xFF);
    }
    
    public static String toString(byte b) {
        return Short.toString(getAsShort(b));
    }
    

    /* Non-static methods */
    
    public byte getSignedByte() {
        return b;
    }
    
    
    /* Non-static method overrides */
    
    public short getAsShort() {
        return u_b;
    }
    
    @Override
    public String toString() {
        return Short.toString(u_b);
    }
}
