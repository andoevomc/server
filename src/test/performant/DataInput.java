/**
 * Copyright � 2005 Punch Entertainment Inc.  All Rights Reserved.
 */
package test.performant;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;

/**
 * mlib2_IDataInput<br>
 *
 * Platform-independent interface for handling input from a data stream.
 * @since mlib2
 */
public class DataInput
{
    ///////////////////////////////////////////////////////////////////////////////////////////////
    //  // Return the number of bytes available to be read.
    //  public int available();
    //
    //  // Read up to aLength bytes into the array.  Return the total number of bytes read.
    //  public int read(byte[] aByteArray, int aOffset, int aLength);
    //
    //  // Read a boolean from the input.
    //  public boolean readBoolean();
    //
    //  // Read a byte.  A byte is 0 <= x <= 255.
    //  public byte readByte();
    //
    //  // Read a string.  The returned string must be freed.  DEPRECATED.  Use readUTF().
    //  public String readString();
    //
    //  // Read a short.  A short is 2 bytes.
    //  public short readShort();
    //
    //  // Read a long.  A long is 4 bytes.
    //  public int readLong();
    //
    //  // Read a UTF string.
    //  public String readUTF();
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // The input stream.
    private DataInputStream mIn;
    // Constructor.
    /**
     * Creates an <code>DataInput</code> object with the initial byte array of data.
     *
     * @param aBytes    The array of bytes holding the initial data.
     */
    public DataInput(byte[] aBytes)
    {
        mIn = new DataInputStream(new ByteArrayInputStream(aBytes));
    }

    //Nhungntp
    public DataInput(byte[] aBytes, int size)
    {
        mIn = new DataInputStream(new ByteArrayInputStream(aBytes, 0, size));
    }
    //huynv edited
    public DataInput(byte[] aBytes, int offset, int size)
    {
        mIn = new DataInputStream(new ByteArrayInputStream(aBytes, offset, size));
    }
    //END
    // Constructor.
    /**
     * Creates an DataInput object with the input stream object.
     *
     * @param aInputStream  The input stream holding the initial data.
     */
    public DataInput(InputStream aInputStream)
    {
        mIn = new DataInputStream(aInputStream);
    }

    /**
     * Return the number of bytes available to be read.
     *
     * @return  Returns number of bytes available to be read. <code>0</code> if error
     * @exception Throwable
     */
    public int available() //throws IOException 
    {
        try
        {
            return mIn.available();
        } catch (Throwable t)
        {
            t.printStackTrace();
            return 0;
        }
    }

    /**
     * Read up to aLength bytes into the array.
     *
     * @param aByteArray    The byte array to store the read data stream into.
     *
     * @param aOffset       The start offset of the data.
     *
     * @param aLength       The maximum number of bytes read.
     *
     * @return the total number of bytes read. <code>0</code> if there is no
     * more data because the end of the stream has been reached.
     *
     * @exception Throwable
     */
    public int read(byte[] aByteArray, int aOffset, int aLength)
    {
        try
        {
            return mIn.read(aByteArray, aOffset, aLength);
        } catch (Throwable t)
        {
            t.printStackTrace();
            return 0;
        }
    }

    /**
     * Reads one input byte and returns true if that byte is nonzero, false if that byte is zero.
     * This method is suitable for reading the byte written by the <code>writeBoolean</code>
     * method of <code>mlib2_DataOutput</code>.
     *
     * @return  the boolean value read.
     * @exception Throwable
     */
    public boolean readBoolean()
    {
        try
        {
            return mIn.readBoolean();
        } catch (Throwable t)
        {
            t.printStackTrace();
            return false;
        }
    }

    /**
     * Reads and returns one input byte. The byte is treated as a signed value
     * in the range <code>0</code> through <code>255</code>, inclusive. This method
     * is suitable for reading the byte written by the <code>writeByte</code>
     * method of <code>mlib2_DataOutput</code>.
     *
     * @return the 8-bit value read.
     * @see     mlib2_DataOutput
     * @exception Throwable
     */
    public byte readByte()
    {
        try
        {
            return (byte) mIn.readUnsignedByte();
        } catch (Throwable t)
        {
            t.printStackTrace();
            return 0;
        }
    }

    /**
     * Reads two input bytes and returns a short value. This method is suitable for
     * reading the bytes written by the <code>writeShort</code> method
     * of <code>mlib2_DataOutput</code>.
     *
     * @return the 16-bit value read. <code>0</code> if there's an error.
     * @see     mlib2_DataOutput
     * @exception Throable
     */
    public short readShort()
    {
        try
        {
            return mIn.readShort();
        } catch (Throwable t)
        {
            t.printStackTrace();
            return 0;
        }
    }

    /**
     * Read a long. A long is 4 bytes. This method is suitable for reading bytes
     * written by the <code>writeLong</code> method of <code>mlib2_IDataOutput</code>.
     *
     * @return the <code>long</code> value read. <code>0</code> if there's an error.
     * @see     mlib2_DataOutput
     * @exception Throwable
     */
    public int readInt()
    {
        try
        {
            return mIn.readInt();
        } catch (Throwable t)
        {
            t.printStackTrace();
            return 0;
        }
    }

    /**
     * Read a UTF string.  The returned string must be freed.
     *
     * @return a Unicode String
     * @see #readString()
     * @exception Throwable
     */
    public String readUTF()
    {
        try
        {
            return mIn.readUTF();
        } catch (Throwable t)
        {
            t.printStackTrace();
            return null;
        }
    }

    public DataInputStream getInternalStream()
    {
        return mIn;
    }

    /**
     * Skip a number of bytes from the begining of the file
     * @param aNumBytes a number of bytes
     */
    public int skip(int aNumBytes)
    {
        try
        {
            return mIn.skipBytes(aNumBytes);
        } catch (Throwable e)
        {
            return 0;
        }

    }

    public long readLong()
    {
        try
        {
            return mIn.readLong();
        } catch (Throwable t)
        {
            t.printStackTrace();
            return 0;
        }
    }
}