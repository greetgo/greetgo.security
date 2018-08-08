package kz.greetgo.security.password;

public interface PasswordEncoder {
  String encode(String password);

  boolean verify(String password, String encodedPassword);
}
