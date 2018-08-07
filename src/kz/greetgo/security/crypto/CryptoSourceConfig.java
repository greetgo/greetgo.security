package kz.greetgo.security.crypto;

public interface CryptoSourceConfig {
  String secureRandomAlgorithm();
  
  String messageDigestAlgorithm();
  
  String keyPairGeneratorAlgorithm();
  
  String cipherAlgorithm();
  
  String keyFactoryAlgorithm();

  int blockSize();
}
