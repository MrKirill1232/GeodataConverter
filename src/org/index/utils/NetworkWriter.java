package org.index.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author Index
 */
public class NetworkWriter
{
    private final ByteArrayOutputStream _byteArray;
    private boolean _reverseBytes = true;

    public NetworkWriter()
    {
        _byteArray = new ByteArrayOutputStream();
    }

    public NetworkWriter(int length)
    {
        _byteArray = new ByteArrayOutputStream(length);
    }

    public void reverseBytes(boolean value)
    {
        _reverseBytes = value;
    }

    public void writeChar(char value)
    {
        final ByteBuffer buf = ByteBuffer.allocate(Character.BYTES);
        buf.putChar(value);
        buf.rewind();
        writeOrHandleException(buf.array());
    }

    public void writeByte(int value)
    {
        final ByteBuffer buf = ByteBuffer.allocate(Byte.BYTES);
        buf.put(0, (byte) value);
        buf.rewind();
        writeOrHandleException(buf.array());
    }

    public void writeShort(int value)
    {
        final ByteBuffer buf = ByteBuffer.allocate(Short.BYTES);
        buf.putShort(0, (short) value);
        buf.rewind();
        writeOrHandleException(reverseByteArray(buf.array()));
    }

    public void writeInt(int value)
    {
        final ByteBuffer buf = ByteBuffer.allocate(Integer.BYTES);
        buf.putInt(0, value);
        buf.rewind();
        writeOrHandleException(reverseByteArray(buf.array()));
    }

    public void writeLong(long value)
    {
        final ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);
        buf.putLong(0, value);
        buf.rewind();
        writeOrHandleException(reverseByteArray(buf.array()));
    }

    public void writeFloat(float value)
    {
        final ByteBuffer buf = ByteBuffer.allocate(Float.BYTES);
        buf.putFloat(0, value);
        buf.rewind();
        writeOrHandleException(reverseByteArray(buf.array()));
    }

    public void writeDouble(double value)
    {
        final ByteBuffer buf = ByteBuffer.allocate(Double.BYTES);
        buf.putDouble(0, value);
        buf.rewind();
        writeOrHandleException(reverseByteArray(buf.array()));
    }

    public void writeBoolean(boolean value)
    {
        writeBooleanAsByte(value, 1, 0);
    }

    public void writeBooleanAsByte(boolean value, int trueValue, int falseValue)
    {
        writeByte(value ? trueValue : falseValue);
    }

    public byte[] getWrittenBytes()
    {
        return Arrays.copyOf(_byteArray.toByteArray(), _byteArray.size());
    }

    private void writeOrHandleException(byte[] inputArray)
    {
        try
        {
            _byteArray.write(inputArray);
        }
        catch (IOException ioE)
        {
            ioE.printStackTrace();
        }
    }

//    private static byte[] concentrateArrays(byte[] inputArray, byte[] additionalArray)
//    {
//        byte[] result = new byte[inputArray.length + additionalArray.length];
//
//        System.arraycopy(inputArray, 0, result, 0, inputArray.length);
//        System.arraycopy(additionalArray, 0, result, inputArray.length, additionalArray.length);
//
//        return result;
//    }

    private byte[] reverseByteArray(byte[] inputArray)
    {
        if (!_reverseBytes)
        {
           return inputArray;
        }

        byte[] result = new byte[inputArray.length];

        for (int index = inputArray.length; index > 0; index--)
        {
            result[inputArray.length - index] = inputArray[index - 1];
        }

        return result;
    }
}
