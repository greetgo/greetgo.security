package kz.greetgo.security.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOStreamUtil {

  @SuppressWarnings("UnusedReturnValue")
  public static OutputStream copyStreams(InputStream in, OutputStream out) {
    try {
      byte buffer[] = new byte[4 * 1024];

      while (true) {
        int read = in.read(buffer);
        if (read < 0) break;
        out.write(buffer, 0, read);
      }

      return out;
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      try {
        in.close();
      } catch (IOException e) {
        //noinspection ThrowFromFinallyBlock
        throw new RuntimeException(e);
      }
    }
  }

}
