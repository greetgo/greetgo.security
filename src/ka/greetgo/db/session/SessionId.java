package ka.greetgo.db.session;

public class SessionId {
  final String salt, part;

  public SessionId(String salt, String part) {
    this.salt = salt;
    this.part = part;
  }

  @Override
  public String toString() {
    return salt + "-" + part;
  }

  public static SessionId parse(String sessionId) {
    if (sessionId == null) return null;
    int i = sessionId.indexOf("-");
    if (i < 0) return null;
    return new SessionId(sessionId.substring(0, i), sessionId.substring(i + 1));
  }
}
