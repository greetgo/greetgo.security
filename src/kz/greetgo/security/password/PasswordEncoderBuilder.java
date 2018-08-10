package kz.greetgo.security.password;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordEncoderBuilder {

  PasswordEncoderBuilder() {}

  public static PasswordEncoderBuilder newBuilder() {
    return new PasswordEncoderBuilder();
  }

  String salt = null;

  public PasswordEncoderBuilder setSalt(String salt) {
    checkBuilt();
    this.salt = salt;
    return this;
  }

  private void checkBuilt() {
    if (built) throw new RuntimeException("Already built");
  }

  private boolean built = false;

  public PasswordEncoder build() {
    if (salt == null) throw new RuntimeException("Please, define salt calling method 'setSalt(...)'");
    built = true;
    return new PasswordEncoder() {
      @Override
      public String encode(String password) {
        try {

          MessageDigest digest = MessageDigest.getInstance("SHA-256");

          digest.update(salt.getBytes(StandardCharsets.UTF_8));

          if (password != null) {
            digest.update(password.getBytes(StandardCharsets.UTF_8));
          }

          String base64 = DatatypeConverter.printBase64Binary(digest.digest());
          base64 = base64.replace('/', '$');
          base64 = base64.replace('+', '~');
          base64 = base64.substring(0, base64.length() - 1);

          return base64;

        } catch (NoSuchAlgorithmException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public boolean verify(String password, String encodedPassword) {
        return encode(password).equals(encodedPassword);
      }
    };
  }
}
