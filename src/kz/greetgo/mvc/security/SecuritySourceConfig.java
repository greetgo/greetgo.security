package kz.greetgo.mvc.security;

public interface SecuritySourceConfig {
  String secureRandomAlgorithm();
  
  String messageDigestAlgorithm();
  
  String keyPairGeneratorAlgorithm();
  
  String cipherAlgorithm();
  
  String keyFactoryAlgorithm();
}
