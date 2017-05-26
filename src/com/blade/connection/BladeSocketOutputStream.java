package com.blade.connection;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author: cold_blade
 * @date: 2017年5月24日
 * @version 1.0
 */
public final class BladeSocketOutputStream extends BufferedOutputStream {

  public BladeSocketOutputStream(OutputStream out) {
    super(out);
  }

  public void writePadBytes(int len) throws IOException {
    for (int i = 0; i < len; i++) {
      writeByte(0);
    }
  }

  /**
   * write an Unicode type string with an end char
   * 
   * @param str
   * @throws IOException
   */
  public void writeUTFString(String str) throws IOException {
    writeUTFString(str, true);
  }

  /**
   * write an Unicode type string
   * 
   * @param str
   * @param appendEndChar whether append an end char
   * @throws IOException
   */
  public void writeUTFString(String str, boolean appendEndChar) throws IOException {
    if (null == str) {
      return;
    }
    int strLen = str.length();
    for (int i = 0; i < strLen; i++) {
      writeChar(str.charAt(i));
    }
    if (appendEndChar) {
      writeChar('\0');
    }
  }

  /**
   * write an ANSI type string with an end char
   * 
   * @param str
   * @throws IOException
   */
  public void writeANSIString(String str) throws IOException {
    writeANSIString(str, true);
  }

  /**
   * write an ANSI type string
   * 
   * @param str
   * @param appendEndChar
   * @throws IOException
   */
  public void writeANSIString(String str, boolean appendEndChar) throws IOException {
    if (null == str) {
      return;
    }
    byte[] value = str.getBytes();
    write(value);
    if (appendEndChar) {
      writeByte(0);
    }
  }

  public void writeByte(int byteValue) throws IOException {
    write(byteValue);
  }

  public void writeBoolean(boolean bValue) throws IOException {
    write(bValue ? 1 : 0);
  }

  public void writeShort(int sValue) throws IOException {
    write((sValue >>> 8) & 0xFF);
    write((sValue >>> 0) & 0xFF);
  }

  public void writeChar(int cValue) throws IOException {
    write((cValue >>> 8) & 0xFF);
    write((cValue >>> 0) & 0xFF);
  }

  public void writeInt(int v) throws IOException {
    write((v >>> 24) & 0xFF);
    write((v >>> 16) & 0xFF);
    write((v >>> 8) & 0xFF);
    write((v >>> 0) & 0xFF);
  }

  public void writeLong(long v) throws IOException {
    byte writeBuffer[] = new byte[8];
    writeBuffer[0] = (byte) (v >>> 56);
    writeBuffer[1] = (byte) (v >>> 48);
    writeBuffer[2] = (byte) (v >>> 40);
    writeBuffer[3] = (byte) (v >>> 32);
    writeBuffer[4] = (byte) (v >>> 24);
    writeBuffer[5] = (byte) (v >>> 16);
    writeBuffer[6] = (byte) (v >>> 8);
    writeBuffer[7] = (byte) (v >>> 0);
    write(writeBuffer, 0, 8);
  }

  public void writeFloat(float v) throws IOException {
    writeInt(Float.floatToIntBits(v));
  }

  public void writeDouble(double v) throws IOException {
    writeLong(Double.doubleToLongBits(v));
  }
}
