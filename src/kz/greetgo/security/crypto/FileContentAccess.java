package kz.greetgo.security.crypto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileContentAccess implements ContentAccess {

  private final File file;

  public FileContentAccess(File file) {
    this.file = file;
  }

  @Override
  public byte[] downloadBytes() {
    try {
      return Files.readAllBytes(file.toPath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void uploadBytes(byte[] bytes) {
    if (bytes == null) {
      file.delete();
      return;
    }

    try {
      file.getParentFile().mkdirs();
      Files.write(file.toPath(), bytes);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean exists() {
    return file.exists();
  }
}
