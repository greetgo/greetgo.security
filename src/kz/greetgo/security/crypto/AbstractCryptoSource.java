package kz.greetgo.security.crypto;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public abstract class AbstractCryptoSource implements CryptoSource {

  protected int getKeySize() {
    return conf().keySize();
  }

  protected abstract byte[] getPrivateKeyBytes();

  protected abstract void setPrivateKeyBytes(byte[] bytes);

  protected abstract byte[] getPublicKeyBytes();

  protected abstract void setPublicKeyBytes(byte[] bytes);

  protected abstract boolean hasKeys();

  protected abstract CryptoSourceConfig conf();

  @Override
  public int getBlockSize() {
    return conf().blockSize();
  }

  @Override
  public Cipher getCipher() {
    try {
      return Cipher.getInstance(conf().cipherAlgorithm());
    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
      throw new RuntimeException(e);
    }
  }

  private final ThreadLocal<PublicKey> publicKey = new ThreadLocal<>();
  private final ThreadLocal<PrivateKey> privateKey = new ThreadLocal<>();

  private void prepareKeys() {
    if (privateKey.get() != null || publicKey.get() != null) return;

    synchronized (this) {
      if (privateKey.get() != null || publicKey.get() != null) return;
      doPrepareKeys();
    }
  }

  @Override
  public PublicKey getPublicKey() {
    prepareKeys();
    return publicKey.get();
  }

  @Override
  public PrivateKey getPrivateKey() {
    prepareKeys();
    return privateKey.get();
  }

  private final ThreadLocal<SecureRandom> random = new ThreadLocal<>();

  @Override
  public SecureRandom getRandom() {
    if (random.get() != null) return random.get();
    try {
      SecureRandom instance = SecureRandom.getInstance(conf().secureRandomAlgorithm());
      random.set(instance);
      return instance;
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  private final ThreadLocal<MessageDigest> messageDigest = new ThreadLocal<>();

  @Override
  public MessageDigest getMessageDigest() {
    {
      MessageDigest result = messageDigest.get();
      if (result != null) return result;
    }

    try {
      MessageDigest instance = MessageDigest.getInstance(conf().messageDigestAlgorithm());
      messageDigest.set(instance);
      return instance;
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  protected void doPrepareKeys() {
    if (hasKeys()) {
      try {
        readKeysFromFiles();
        return;
      } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
        generateKeys();
        saveKeys();
      }
    }
  }

  protected void saveKeys() {
    {
      final PKCS8EncodedKeySpec privateKetSpec = new PKCS8EncodedKeySpec(privateKey.get()
        .getEncoded());
      setPrivateKeyBytes(privateKetSpec.getEncoded());
    }
    {
      X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKey.get().getEncoded());
      setPublicKeyBytes(publicSpec.getEncoded());
    }
  }

  protected void generateKeys() {
    try {

      final KeyPairGenerator kpg = KeyPairGenerator.getInstance(conf().keyPairGeneratorAlgorithm());

      kpg.initialize(getKeySize(), getRandom());

      final KeyPair keyPair = kpg.generateKeyPair();

      privateKey.set(keyPair.getPrivate());
      publicKey.set(keyPair.getPublic());

    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  protected void readKeysFromFiles() throws NoSuchAlgorithmException, InvalidKeySpecException {

    final PKCS8EncodedKeySpec keySpecPrivate = new PKCS8EncodedKeySpec(getPrivateKeyBytes());
    final X509EncodedKeySpec keySpecPublic = new X509EncodedKeySpec(getPublicKeyBytes());

    final KeyFactory keyFactory = KeyFactory.getInstance(conf().keyFactoryAlgorithm());

    privateKey.set(keyFactory.generatePrivate(keySpecPrivate));
    publicKey.set(keyFactory.generatePublic(keySpecPublic));

  }
}
