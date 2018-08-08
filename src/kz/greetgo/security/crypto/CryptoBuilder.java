package kz.greetgo.security.crypto;

import kz.greetgo.db.DbType;
import kz.greetgo.db.Jdbc;

import java.io.File;
import java.util.Objects;

public class CryptoBuilder {
  CryptoBuilder() {}

  public static CryptoBuilder newBuilder() {
    return new CryptoBuilder();
  }

  CryptoSourceConfig cryptoSourceConfig = new CryptoSourceConfigDefault();

  int keySize = 1024;

  /**
   * Defines key size
   *
   * @param keySize key size
   * @return reference to this
   */
  public CryptoBuilder setKeySize(int keySize) {
    this.keySize = keySize;
    return this;
  }

  /**
   * Defines crypto source to store keys in files. Files automatically randomly generates at first time
   *
   * @param privateKeyFile private key file
   * @param publicKeyFile  public key file
   * @return reference to this builder
   */
  public CryptoBuilderKeysInFiles inFiles(File privateKeyFile, File publicKeyFile) {
    return new CryptoBuilderKeysInFiles(this, privateKeyFile, publicKeyFile);
  }

  /**
   * Defines crypto config, where you can specify using algorithms
   *
   * @param cryptoSourceConfig config for crypto source
   * @return reference to this builder
   */
  @SuppressWarnings("UnusedReturnValue")
  public CryptoBuilder setConfig(CryptoSourceConfig cryptoSourceConfig) {
    this.cryptoSourceConfig = cryptoSourceConfig;
    return this;
  }

  /**
   * Builds on custom access to keys content
   *
   * @param privateKeyAccess access to private key
   * @param publicKeyAccess  access to public key
   * @return built object
   */
  public Crypto build(ContentAccess privateKeyAccess, ContentAccess publicKeyAccess) {
    Objects.requireNonNull(privateKeyAccess);
    Objects.requireNonNull(publicKeyAccess);
    return new CryptoBridge(new CryptoSourceImpl(cryptoSourceConfig, privateKeyAccess, publicKeyAccess, keySize));
  }

  public CryptoBuilderKeysInDb inDb(DbType dbType, Jdbc jdbc) {
    Objects.requireNonNull(dbType);
    Objects.requireNonNull(jdbc);
    return new CryptoBuilderKeysInDb(this, dbType, jdbc);
  }
}
