package kz.greetgo.security.util;

public class ByteUtil {
  public static byte[] xorBytes(byte[] bytes1, byte[] bytes2) {
    if (bytes1 == null) {
      bytes1 = new byte[0];
    }
    if (bytes2 == null) {
      bytes2 = new byte[0];
    }
    int minSize = Math.min(bytes1.length, bytes2.length);
    int maxSize = Math.max(bytes1.length, bytes2.length);
    byte[] result = new byte[maxSize];
    for (int i = 0; i < minSize; i++) {
      result[i] = (byte) (bytes1[i] ^ bytes2[i]);
    }

    if (bytes1.length > bytes2.length) {
      System.arraycopy(bytes1, minSize, result, minSize, maxSize - minSize);
    }
    if (bytes1.length < bytes2.length) {
      System.arraycopy(bytes2, minSize, result, minSize, maxSize - minSize);
    }
    return result;
  }

  public static byte[] copyToLength(byte[] source, int length) {
    if (length < 0) {
      throw new IllegalArgumentException("length = " + length + " MUST be more then or equal to zero");
    }

    int sourceLength = source.length;

    if (sourceLength == length) {
      return source;
    }

    if (length < sourceLength) {
      byte[] ret = new byte[length];
      System.arraycopy(source, 0, ret, 0, length);
      return ret;
    }

    {
      byte[] ret = new byte[length];
      for (int i = 0; i < length; i++) {
        ret[i] = source[i % sourceLength];
      }
      return ret;
    }
  }
}
