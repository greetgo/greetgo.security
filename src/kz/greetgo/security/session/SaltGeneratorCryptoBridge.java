package kz.greetgo.security.session;

import kz.greetgo.security.crypto.Crypto;
import org.bson.internal.Base64;

import java.nio.charset.StandardCharsets;

public class SaltGeneratorCryptoBridge implements SaltGenerator {
  private final Crypto crypto;
  private final int saltLength;

  public SaltGeneratorCryptoBridge(Crypto crypto, int saltLength) {
    this.crypto = crypto;
    this.saltLength = saltLength;
  }

  @Override
  public String generateSalt(String str) {
    byte[] saltBytes = crypto.encrypt(str.getBytes(StandardCharsets.UTF_8));

    String salt = Base64.encode(saltBytes);
    salt = salt.replace('/', '$').replace('+', '~');
    salt = salt.substring(0, salt.length() - 1);

    return
      saltLength > 0 && saltLength < salt.length()
        ? salt.substring(0, saltLength)
        : salt;
  }
}
