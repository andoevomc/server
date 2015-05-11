/**
 * Copyright � 2005 Punch Entertainment Inc.  All Rights Reserved.
 */
package test.performant;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

/**
 * mlib2_IDataOutput<br>
 *
 * Platform-independent interface for handling output to a data stream.
 */
public class DataOutput {
    ///////////////////////////////////////////////////////////////////////////////////////////////
    //  // Write aLength bytes from the specified byte array.
    //  public void write(byte[] aByteArray, int aOffset, int aLength);
    //
    //  // Write a boolean to the underlying output.
    //  public void writeBoolean(boolean aBool);
    //
    //  // Write a byte.  A byte is 0 <= x <= 255.
    //  public void writeByte(int aByte);
    //
    //  // Write a string.  DEPRECTED.  Use writeUTF().
    //  public void writeString(String aString);
    //
    //  // Write a short.  2 bytes.
    //  public void writeShort(short aShort);
    //
    //  // Write a long.  4 bytes.
    //  public void writeLong(int aLong);
    //
    //  // Write a UTF string.
    //  public void writeUTF(String aString);
    //
    //  // Return a pointer to the data.
    //  public byte[] getBytes();
    //
    //  // Return the number of bytes written.
    //  public int size();
    //
    //  // Reset the object.  The size becomes zero.
    //  public void reset();
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // The data output stream.

    private DataOutputStream mOut;    // The byte array output stream.
    private ByteArrayOutputStream mData;
    // Constructor.

    /**
     * Creates a new data output stream to write data to.
     */
    public DataOutput() {
        mData = new ByteArrayOutputStream();
        mOut = new DataOutputStream(mData);
    }

    public DataOutput(int param) {
        //Tuan Bui Added
        mData = new ByteArrayOutputStream();
        mOut = new DataOutputStream(mData);
    }

    /**
     * Writes <code>aLength</code> bytes from the specified byte array starting at offset
     * <code>aOffset</code> to the underlying output stream.
     *
     * @param aByteArray    The data.
     *
     * @param aOffset       The start offset in the data.
     *
     * @param aLength       The number of bytes to write.
     */
    public void write(byte[] aByteArray, int aOffset, int aLength) {
        try {
            mOut.write(aByteArray, aOffset, aLength);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Writes <code>aLength</code> bytes from the specified byte array starting at offset
     * <code>aOffset</code> to the underlying output stream.
     *
     * @param aByteArray    The data.
     *
     * @param aOffset       The start offset in the data.
     *
     * @param aLength       The number of bytes to write.
     */
    public void write(byte[] aByteArray) {
        int aOffset = 0;
        int aLength = (aByteArray == null) ? 0 : aByteArray.length;
        try {
            mOut.write(aByteArray, aOffset, aLength);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Writes a <code>boolean</code> to the underlying output stream as a 1-byte
     * value. The value <code>true</code> is written out as the value <code>(byte)1;
     * the value <code>false</code> is written out as the value <code>(byte)0</code>.
     *
     * @param aBool A <code>boolean</code> value to be written.
     */
    public void writeBoolean(boolean aBool) {
        try {
            mOut.writeBoolean(aBool);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Writes out a <code>byte</code> to the underlying output stream
     * as a 1-byte value. A <code>byte</code> is 0 &lt= x &lt= 255.
     *
     * @param aByte a <code>byte</code> value to be written.
     */
    public void writeByte(int aByte) {
        try {
            mOut.writeByte(aByte);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Writes a string to the underlying output stream using ANSI encoding in
     * a machine-independent manner.
     *
     * @param aString   a <code>string</code> to be written.
     */
    public void writeChars(String aString) {
        try {
            mOut.writeChars(aString);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Writes a string to the underlying output stream using UTF-8 encoding in
     * a machine-independent manner.
     *
     * @param aString   a <code>string</code> to be written.
     */
    public void writeShort(short aShort) {
        try {
            mOut.writeShort(aShort);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Writes a long to the underlying output stream as four bytes.
     *
     * @param aLong     a <code>long</code> to be written
     */
    public void writeInt(int aInt) {
        try {
            mOut.writeInt(aInt);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    // Write a UTF string.

    /**
     * Writes a Unicode string.
     * @see #writeString(String aString)
     */
    public void writeUTF(String aString) {
        try {
            mOut.writeUTF(aString);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * The getBytes method returns a pointer to the data
     *
     * @return pointer to the data as a <code>byte</code> array. Null if failed.
     */
    public byte[] getBytes() {
        try {
            mOut.flush();
            return mData.toByteArray();
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Returns the number of bytes written to the output stream.
     *
     * @return the number of bytes written. 0 if error.
     */
    public int size() {
        try {
            mOut.flush();
            return mData.size();
        } catch (Throwable t) {
            t.printStackTrace();
            return 0;
        }
    }

    /**
     * Resets the output stream, setting the size to zero.
     */
    public void reset() {
        try {
            mOut.flush();
            mData.reset();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void writeLong(long aLong) {
        try {
            mOut.writeLong(aLong);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
