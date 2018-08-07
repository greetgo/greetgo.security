package kz.greetgo.security.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileUtil {
  public static void strToFile(String str, File file) {
    try {
      Files.write(file.toPath(), str.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
