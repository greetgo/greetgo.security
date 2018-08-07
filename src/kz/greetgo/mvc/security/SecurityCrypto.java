package kz.greetgo.mvc.security;

public interface SecurityCrypto {
  byte[] encrypt(byte[] bytes);
  
  byte[] decrypt(byte[] encryptedBytes);

  byte[] sign(byte[] bytes);
  
  boolean verifySignature(byte[] bytes, byte[] signature);
}
