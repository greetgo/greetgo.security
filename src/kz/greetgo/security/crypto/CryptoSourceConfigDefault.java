package kz.greetgo.security.crypto;

public class CryptoSourceConfigDefault implements CryptoSourceConfig {
  @Override
  public String secureRandomAlgorithm() {
    //noinspection SpellCheckingInspection
    return "SHA1PRNG";
  }

  @Override
  public String messageDigestAlgorithm() {
    return "SHA-256";
  }

  @Override
  public String keyPairGeneratorAlgorithm() {
    return "RSA";
  }

  @Override
  public String cipherAlgorithm() {
    return "RSA";
  }

  @Override
  public String keyFactoryAlgorithm() {
    return "RSA";
  }

  @Override
  public int blockSize() {
    return 117;
  }
}
