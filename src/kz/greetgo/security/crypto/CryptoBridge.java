package kz.greetgo.security.crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class CryptoBridge implements Crypto {

  public static CryptoTrace trace = null;

  private final CryptoSource cryptoSource;

  public CryptoBridge(CryptoSource cryptoSource) {
    this.cryptoSource = cryptoSource;
  }

  private static byte[] encryptBlock(byte[] bytes, CryptoSource cryptoSource) {
    try {
      Cipher cipher = cryptoSource.getCipher();
      cipher.init(Cipher.ENCRYPT_MODE, cryptoSource.getPublicKey());
      return cipher.doFinal(bytes);
    } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
      if (trace != null) trace.trace("CP 24WwTRF26U", e);
      throw new RuntimeException(e);
    }
  }

  private static byte[] decryptBlock(byte[] encryptedBytes, CryptoSource cryptoSource) {
    try {

      Cipher cipher = cryptoSource.getCipher();
      cipher.init(Cipher.DECRYPT_MODE, cryptoSource.getPrivateKey());
      return cipher.doFinal(encryptedBytes);

    } catch (BadPaddingException | IllegalBlockSizeException e) {
      if (trace != null) trace.trace("CP A5dm6oys4i", e);
      return null;
    } catch (InvalidKeyException e) {
      if (trace != null) trace.trace("CP txLAlobOu7", e);
      throw new RuntimeException(e);
    }
  }

  private interface EncryptedData {
    void encryptAndSet(byte[] bytes, CryptoSource cryptoSource);

    byte[] decryptAndGet(CryptoSource cryptoSource);
  }

  private static EncryptedData createEncryptedData(byte[] bytes, CryptoSource cryptoSource) {
    if (bytes == null) return null;
    final EncryptedData ret;
    if (cryptoSource.getBlockSize() < bytes.length) {
      ret = new ManyBlocks();
    } else {
      ret = new SmallBlock();
    }
    ret.encryptAndSet(bytes, cryptoSource);
    return ret;
  }

  private static class SmallBlock implements EncryptedData, Serializable {

    byte[] encryptedBytes;

    @Override
    public void encryptAndSet(byte[] bytes, CryptoSource cryptoSource) {
      encryptedBytes = encryptBlock(bytes, cryptoSource);
    }

    @Override
    public byte[] decryptAndGet(CryptoSource cryptoSource) {
      return decryptBlock(encryptedBytes, cryptoSource);
    }
  }

  private static class ManyBlocks implements EncryptedData, Serializable {

    byte[] encryptedSymmetricKey;

    final List<byte[]> blockList = new ArrayList<>();

    @Override
    public void encryptAndSet(byte[] bytes, CryptoSource cryptoSource) {
      final byte[] symmetricKey = new byte[cryptoSource.getBlockSize()];
      cryptoSource.getRandom().nextBytes(symmetricKey);

      encryptedSymmetricKey = encryptBlock(symmetricKey, cryptoSource);

      writeToBlockList(blockList, symmetricKey, bytes);
    }

    @Override
    public byte[] decryptAndGet(CryptoSource cryptoSource) {
      final byte[] symmetricKey = decryptBlock(encryptedSymmetricKey, cryptoSource);
      if (symmetricKey == null) return null;
      return readFromBlockList(blockList, symmetricKey);
    }
  }

  static byte[] readFromBlockList(List<byte[]> blockList, byte[] symmetricKey) {
    int bytesCount = 0;

    for (final byte[] block : blockList) {
      final int blockLength = block.length;
      for (int j = 0; j < blockLength; j++) {
        block[j] ^= symmetricKey[j];
      }
      bytesCount += blockLength;
    }

    byte[] ret = new byte[bytesCount];

    int filledCount = 0;
    for (byte[] block : blockList) {
      int blockLength = block.length;
      System.arraycopy(block, 0, ret, filledCount, blockLength);
      filledCount += blockLength;
    }

    return ret;
  }

  static void writeToBlockList(List<byte[]> blockList, byte[] symmetricKey, byte[] bytes) {
    int performedCount = 0;

    final int bytesLength = bytes.length;
    final int symmetricKeyLength = symmetricKey.length;

    while (performedCount < bytesLength) {
      int performBorder = performedCount + symmetricKeyLength;
      if (performBorder > bytesLength) performBorder = bytesLength;
      int currentBlockSize = performBorder - performedCount;
      byte[] block = new byte[currentBlockSize];
      System.arraycopy(bytes, performedCount, block, 0, currentBlockSize);
      for (int i = 0; i < currentBlockSize; i++) {
        block[i] ^= symmetricKey[i];
      }
      blockList.add(block);
      performedCount = performBorder;
    }
  }

  @Override
  public byte[] encrypt(byte[] bytes) {
    EncryptedData encryptedData = createEncryptedData(bytes, cryptoSource);
    if (encryptedData == null) return null;

    ByteArrayOutputStream bOut = new ByteArrayOutputStream();

    try (ObjectOutputStream oos = new ObjectOutputStream(bOut)) {
      oos.writeObject(encryptedData);
    } catch (IOException e) {
      if (trace != null) trace.trace("CP tToHF5T7vr", e);
      throw new RuntimeException(e);
    }

    return bOut.toByteArray();
  }

  @Override
  public byte[] decrypt(byte[] encryptedBytes) {
    if (encryptedBytes == null) return null;

    ByteArrayInputStream bIn = new ByteArrayInputStream(encryptedBytes);
    try (ObjectInputStream ois = new ObjectInputStream(bIn)) {

      final EncryptedData encryptedData = (EncryptedData) ois.readObject();

      return encryptedData.decryptAndGet(cryptoSource);

    } catch (IOException | ClassNotFoundException | ClassCastException e) {
      if (trace != null) trace.trace("CP 6c37UGb42g", e);
      return null;
    }

  }

  @Override
  public byte[] sign(byte[] bytes) {
    if (bytes == null) return null;
    try {

      final byte[] hash1 = cryptoSource.getMessageDigest().digest(bytes);

      Cipher cipher = cryptoSource.getCipher();
      cipher.init(Cipher.ENCRYPT_MODE, cryptoSource.getPrivateKey());

      return cipher.doFinal(hash1);

    } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
      if (trace != null) trace.trace("CP 7xTj50ch06", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean verifySignature(byte[] bytes, byte[] signature) {
    if (signature == null || bytes == null) {
      if (trace != null) trace.trace("CP I1iC54087r");
      return false;
    }
    try {

      final byte[] hash1 = cryptoSource.getMessageDigest().digest(bytes);

      Cipher cipher = cryptoSource.getCipher();
      cipher.init(Cipher.DECRYPT_MODE, cryptoSource.getPublicKey());

      final byte[] hash2 = cipher.doFinal(signature);

      if (hash1.length != hash2.length) {
        if (trace != null) trace.trace("CP DR5Xd1NH19");
        return false;
      }

      for (int i = 0, n = hash1.length; i < n; i++) {
        if (hash1[i] != hash2[i]) {
          if (trace != null) trace.trace("CP 7tJoTkWm0H");
          return false;
        }
      }

      if (trace != null) trace.trace("CP m9t3OpQL0O");
      return true;

    } catch (BadPaddingException | IllegalBlockSizeException | ArrayIndexOutOfBoundsException e) {

      if (trace != null) trace.trace("CP AJ1k7Gy2Hk", e);
      return false;

    } catch (InvalidKeyException e) {
      if (trace != null) trace.trace("CP 08saDm2rsb", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public byte[] makeHash(byte[] sourceBytes) {
    return cryptoSource.getMessageDigest().digest(sourceBytes);
  }

  @Override
  public SecureRandom rnd() {
    return cryptoSource.getRandom();
  }
}
