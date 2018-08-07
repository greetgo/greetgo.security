package kz.greetgo.mvc.security;

import java.io.*;

public class SerializeUtil {
  public static Object deserialize(byte[] bytes) {
    try {
      if (bytes == null) return null;
      final ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
      ObjectInputStream ois = new ObjectInputStream(bin);
      return ois.readObject();
    } catch (EOFException | StreamCorruptedException | InvalidClassException e) {
      return null;
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] serialize(Object object) {
    try {
      if (object == null) return null;

      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bout);
      oos.writeObject(object);
      oos.flush();
      return bout.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
