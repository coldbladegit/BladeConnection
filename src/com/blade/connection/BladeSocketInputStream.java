package com.blade.connection;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author: cold_blade
 * @date: 2017年5月6日
 * @version 1.0
 */
public final class BladeSocketInputStream extends BufferedInputStream {

  public BladeSocketInputStream(InputStream in) {
    super(in);
  }

  /**
   * read an Unicode type string which a char equals two bytes
   * 
   * @param len from the service
   * @return an instance of String
   * @throws IOException
   */
  public String readUTFString(int len) throws IOException {
    int bufLen = len >> 1;
    char buffer[] = new char[len];
    int strLen = 0;
    for (int i = 0; i < bufLen; i++) {
      buffer[i] = readChar();
      if ('\0' != buffer[i]) {
        strLen++;
      }
    }
    return new String(buffer, 0, strLen);
  }

  /**
   * read an ANSI type string
   * 
   * @param len from service
   * @return an instance of String
   * @throws IOException
   */
  public String readANSIString(int len) throws IOException {
    byte[] bytes = new byte[len];
    readFully(bytes);
    int strLen = len;
    for (int i = 0; i < len; ++i) {
      if ('\0' == bytes[i]) {
        strLen = i;
        break;
      }
    }
    return new String(bytes, 0, strLen);
  }

  public void readFully(byte b[]) throws IOException {
    readFully(b, 0, b.length);
  }

  public void readFully(byte b[], int off, int len) throws IOException {
    if (len < 0) {
      throw new IndexOutOfBoundsException();
    }
    int n = 0;
    while (n < len) {
      int count = read(b, off + n, len - n);
      if (count < 0) {
        throw new EOFException();
      }
      n += count;
    }
  }

  public long skipBytes(long n) throws IOException {
    long total = 0;
    while ((total < n)) {
      long count = skip(n - total);
      total += count;
    }
    return total;
  }

  public boolean readBoolean() throws IOException {
    int ch = read();
    if (ch < 0) {
      throw new EOFException();
    }
    return (ch != 0);
  }

  public byte readByte() throws IOException {
    int ch = read();
    if (ch < 0) {
      throw new EOFException();
    }
    return (byte) (ch);
  }

  public int readUnsignedByte() throws IOException {
    int ch = read();
    if (ch < 0) {
      throw new EOFException();
    }
    return ch;
  }

  public short readShort() throws IOException {
    int ch1 = read();
    int ch2 = read();
    if ((ch1 | ch2) < 0)
      throw new EOFException();
    return (short) ((ch1 << 8) + (ch2 << 0));
  }

  public int readUnsignedShort() throws IOException {
    int ch1 = read();
    int ch2 = read();
    if ((ch1 | ch2) < 0)
      throw new EOFException();
    return (ch1 << 8) + (ch2 << 0);
  }

  public char readChar() throws IOException {
    int ch1 = read();
    int ch2 = read();
    if ((ch1 | ch2) < 0)
      throw new EOFException();
    return (char) ((ch1 << 8) + (ch2 << 0));
  }

  public int readInt() throws IOException {
    int ch1 = read();
    int ch2 = read();
    int ch3 = read();
    int ch4 = read();
    if ((ch1 | ch2 | ch3 | ch4) < 0)
      throw new EOFException();
    return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
  }

  public long readLong() throws IOException {
    byte readBuffer[] = new byte[8];
    readFully(readBuffer, 0, 8);
    return (((long) readBuffer[0] << 56) + ((long) (readBuffer[1] & 255) << 48)
        + ((long) (readBuffer[2] & 255) << 40) + ((long) (readBuffer[3] & 255) << 32)
        + ((long) (readBuffer[4] & 255) << 24) + ((readBuffer[5] & 255) << 16)
        + ((readBuffer[6] & 255) << 8) + ((readBuffer[7] & 255) << 0));
  }

  public float readFloat() throws IOException {
    return Float.intBitsToFloat(readInt());
  }

  public double readDouble() throws IOException {
    return Double.longBitsToDouble(readLong());
  }
}
