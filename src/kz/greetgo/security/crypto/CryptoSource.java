package kz.greetgo.security.crypto;

import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.Cipher;

public interface CryptoSource {
  Cipher getCipher();
  
  PublicKey getPublicKey();
  
  PrivateKey getPrivateKey();
  
  MessageDigest getMessageDigest();
  
  SecureRandom getRandom();
  
  int getBlockSize();
}
