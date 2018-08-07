package kz.greetgo.security.crypto;

public interface Crypto {
  byte[] encrypt(byte[] bytes);
  
  byte[] decrypt(byte[] encryptedBytes);

  byte[] sign(byte[] bytes);
  
  boolean verifySignature(byte[] bytes, byte[] signature);
}
