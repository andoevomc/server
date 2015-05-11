/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.util.codec.md5;

import dreamgame.databaseDriven.DatabaseDriver;

/**
 *
 * @author thohd
 */
public class MD5 {

    private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    /**
     * Used to build output as Hex
     */
    private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String md5HexBase(String data) throws Exception {
        return encodeHexString(java.security.MessageDigest.getInstance("MD5").digest(data.getBytes()));
    }

    public static String md5Hex(String data) throws Exception {
        if (DatabaseDriver.doubleMD5) {
            return md5HexBase(md5HexBase(data));
        }

        return md5HexBase(data);
    }

    public static String encodeHexString(byte[] data) {
        return new String(encodeHex(data));
    }

    public static String md5Hex(byte[] data) throws Exception {
        return encodeHexString(java.security.MessageDigest.getInstance("MD5").digest(data));
    }

    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }

    public static char[] encodeHex(byte[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    protected static char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }
}
