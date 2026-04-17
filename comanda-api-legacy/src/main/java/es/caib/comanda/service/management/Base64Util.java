package es.caib.comanda.service.management;

import java.io.UnsupportedEncodingException;

public final class Base64Util {
  private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

  private Base64Util() {
  }

  public static String encodeUtf8(String value) {
    try {
      return encode(value.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException("UTF-8 not supported", e);
    }
  }

  public static String encode(byte[] bytes) {
    if (bytes == null || bytes.length == 0) {
      return "";
    }

    StringBuilder builder = new StringBuilder(((bytes.length + 2) / 3) * 4);
    int index = 0;
    while (index < bytes.length) {
      int first = bytes[index++] & 0xff;
      int second = index < bytes.length ? bytes[index++] & 0xff : -1;
      int third = index < bytes.length ? bytes[index++] & 0xff : -1;

      builder.append(ALPHABET[first >>> 2]);
      builder.append(ALPHABET[((first & 0x03) << 4) | (second >= 0 ? (second >>> 4) : 0)]);
      builder.append(second >= 0 ? ALPHABET[((second & 0x0f) << 2) | (third >= 0 ? (third >>> 6) : 0)] : '=');
      builder.append(third >= 0 ? ALPHABET[third & 0x3f] : '=');
    }
    return builder.toString();
  }
}
