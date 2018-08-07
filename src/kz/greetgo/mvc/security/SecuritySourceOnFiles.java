package kz.greetgo.mvc.security;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import kz.greetgo.util.ServerUtil;

public class SecuritySourceOnFiles extends AbstractSecuritySource {
  
  private final int keySize, blockSize;
  private final File privateKeyFile;
  private final File publicKeyFile;
  private final SecuritySourceConfig conf;
  
  public SecuritySourceOnFiles(int keySize, int blockSize, File privateKeyFile, File publicKeyFile,
      SecuritySourceConfig conf) {
    this.keySize = keySize;
    this.blockSize = blockSize;
    this.privateKeyFile = privateKeyFile;
    this.publicKeyFile = publicKeyFile;
    this.conf = conf;
  }
  
  public SecuritySourceOnFiles(File privateKeyFile, File publicKeyFile, SecuritySourceConfig conf) {
    this(DEFAULT_KEY_SIZE, DEFAULT_BLOCK_SIZE, privateKeyFile, publicKeyFile, conf);
  }
  
  public SecuritySourceOnFiles(File privateKeyFile, File publicKeyFile) {
    this(DEFAULT_KEY_SIZE, DEFAULT_BLOCK_SIZE, privateKeyFile, publicKeyFile,
        new SecuritySourceConfigDefault());
  }
  
  @Override
  protected int getKeySize() {
    return keySize;
  }
  
  @Override
  public int getBlockSize() {
    return blockSize;
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
        ServerUtil.copyStreamsAndCloseIn(in, out);
      }
      return out.toByteArray();
    } catch (Exception e) {
      if (e instanceof RuntimeException) throw (RuntimeException)e;
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
      if (e instanceof RuntimeException) throw (RuntimeException)e;
      throw new RuntimeException(e);
    }
  }
  
  @Override
  protected boolean hasKeys() {
    return privateKeyFile.exists() && publicKeyFile.exists();
  }
  
  @Override
  protected SecuritySourceConfig conf() {
    return conf;
  }
}
