package kz.greetgo.security.session;

public interface SaltGenerator {
  String generateSalt(String str);
}
