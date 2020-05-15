package kz.greetgo.security.session;

import kz.greetgo.security.crypto.Crypto;
import org.bson.internal.Base64;

import java.nio.charset.StandardCharsets;

import static kz.greetgo.security.util.ByteUtil.copyToLength;
import static kz.greetgo.security.util.ByteUtil.xorBytes;

public class SaltGeneratorCryptoBridge implements SaltGenerator {
  private final Crypto crypto;
  private final int saltLength;
  private final byte[] mixtureBytes;

  public SaltGeneratorCryptoBridge(Crypto crypto, int saltLength, byte[] mixtureBytes) {
    this.crypto = crypto;
    this.saltLength = saltLength;
    this.mixtureBytes = mixtureBytes;
  }

  @Override
  public String generateSalt(String str) {
    byte[] beginBytes = str.getBytes(StandardCharsets.UTF_8);
    byte[] sourceBytes = xorBytes(beginBytes, copyToLength(mixtureBytes, beginBytes.length));
    byte[] hashBytes = crypto.makeHash(sourceBytes);

    String salt = Base64.encode(hashBytes);
    salt = salt.replace('/', '$').replace('+', '~');
    while (salt.endsWith("=")) {
      salt = salt.substring(0, salt.length() - 1);
    }

    return
      saltLength > 0 && saltLength < salt.length()
        ? salt.substring(salt.length() - saltLength)
        : salt;
  }
}
