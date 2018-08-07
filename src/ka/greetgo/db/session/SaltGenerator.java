package ka.greetgo.db.session;

public interface SaltGenerator {
  String generateSalt(String str);
}
