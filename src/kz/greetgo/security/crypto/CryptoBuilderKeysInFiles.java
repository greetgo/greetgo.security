package kz.greetgo.security.crypto;

import java.io.File;

public class CryptoBuilderKeysInFiles {

  private final CryptoBuilder parent;
  private final File privateKeyFile;
  private final File publicKeyFile;

  CryptoBuilderKeysInFiles(CryptoBuilder parent, File privateKeyFile, File publicKeyFile) {
    this.parent = parent;
    this.privateKeyFile = privateKeyFile;
    this.publicKeyFile = publicKeyFile;
  }

  /**
   * Defines crypto config, where you can specify using algorithms
   *
   * @param cryptoSourceConfig config for crypto source
   * @return reference to this builder
   */
  public CryptoBuilderKeysInFiles setConfig(CryptoSourceConfig cryptoSourceConfig) {
    parent.cryptoSourceConfig = cryptoSourceConfig;
    return this;
  }

  /**
   * Target builder method - builds a new crypto
   *
   * @return built object
   */
  public Crypto build() {
    return parent.build(new FileContentAccess(privateKeyFile), new FileContentAccess(publicKeyFile));
  }
}
