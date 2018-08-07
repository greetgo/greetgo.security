package kz.greetgo.security.util;

import java.util.Base64;

public class Base64Util {
  private static final char slash = '$';
  private static final char equal = '_';
  private static final char plus = '.';
  
  public static byte[] base64ToBytes(String base64) {
    if (base64 == null) return null;
    try {
      String base64Restored = restoreUnsafeCharacters(base64);
      final byte[] ret = Base64.getDecoder().decode(base64Restored);
      if (ret == null) return null;
      if (ret.length == 0) return null;
      return ret;
    } catch (Exception e) {
      return null;
    }
  }
  
  public static String bytesToBase64(byte[] bytes) {
    if (bytes == null) return null;
    String result = Base64.getEncoder().encodeToString(bytes);
    return escapeUnsafeCharacters(result);
  }
  
  private static String escapeUnsafeCharacters(String result) {
    String ret = result.replace('/', slash);
    ret = ret.replace('=', equal);
    ret = ret.replace('+', plus);
    return ret;
  }
  
  private static String restoreUnsafeCharacters(String result) {
    String ret = result.replace(slash, '/');
    ret = ret.replace(equal, '=');
    ret = ret.replace(plus, '+');
    return ret;
  }
}
