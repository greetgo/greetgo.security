package kz.greetgo.security.crypto;

import java.security.SecureRandom;

public interface Crypto {
  byte[] encrypt(byte[] bytes);

  byte[] decrypt(byte[] encryptedBytes);

  byte[] sign(byte[] bytes);

  boolean verifySignature(byte[] bytes, byte[] signature);

  SecureRandom rnd();
}
