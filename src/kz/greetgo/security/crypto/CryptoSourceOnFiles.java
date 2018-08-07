package kz.greetgo.security.crypto;

import kz.greetgo.security.util.IOStreamUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class CryptoSourceOnFiles extends AbstractCryptoSource {

  private final File privateKeyFile;
  private final File publicKeyFile;
  private final CryptoSourceConfig conf;

  public CryptoSourceOnFiles(File privateKeyFile, File publicKeyFile, CryptoSourceConfig conf) {
    this.privateKeyFile = privateKeyFile;
    this.publicKeyFile = publicKeyFile;
    this.conf = conf;
  }

  public CryptoSourceOnFiles(File privateKeyFile, File publicKeyFile) {
    this(privateKeyFile, publicKeyFile, new CryptoSourceConfigDefault());
  }

  @Override
  protected byte[] getPrivateKeyBytes() {
    return loadFile(privateKeyFile);
  }

  @Override
  protected void setPrivateKeyBytes(byte[] bytes) {
    saveToFile(bytes, privateKeyFile);
  }

  @Override
  protected byte[] getPublicKeyBytes() {
    return loadFile(publicKeyFile);
  }

  @Override
  protected void setPublicKeyBytes(byte[] bytes) {
    saveToFile(bytes, publicKeyFile);
  }

  private byte[] loadFile(File file) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream(getKeySize() * 5);

      try (FileInputStream in = new FileInputStream(file)) {
        IOStreamUtil.copyStreams(in, out);
      }
      return out.toByteArray();
    } catch (Exception e) {
      if (e instanceof RuntimeException) throw (RuntimeException) e;
      throw new RuntimeException(e);
    }
  }

  private static void saveToFile(byte[] bytes, File file) {
    try {
      file.getParentFile().mkdirs();
      try (FileOutputStream out = new FileOutputStream(file)) {
        out.write(bytes);
      }
    } catch (Exception e) {
      if (e instanceof RuntimeException) throw (RuntimeException) e;
      throw new RuntimeException(e);
    }
  }

  @Override
  protected boolean hasKeys() {
    return privateKeyFile.exists() && publicKeyFile.exists();
  }

  @Override
  protected CryptoSourceConfig conf() {
    return conf;
  }
}
