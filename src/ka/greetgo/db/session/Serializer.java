package ka.greetgo.db.session;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serializer {
  public static byte[] serialize(Object object) {
    try {

      ByteArrayOutputStream bOut = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(bOut);

      out.writeObject(object);

      return bOut.toByteArray();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T deserialize(byte[] bytes) {
    try {

      if (bytes == null) return null;
      ByteArrayInputStream bIn = new ByteArrayInputStream(bytes);
      ObjectInputStream in = new ObjectInputStream(bIn);
      //noinspection unchecked
      return (T) in.readObject();

    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }


}
