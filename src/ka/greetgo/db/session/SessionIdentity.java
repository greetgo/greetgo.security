package ka.greetgo.db.session;

public class SessionIdentity {
  public final String id;
  public final String token;

  public SessionIdentity(String id, String token) {
    this.id = id;
    this.token = token;
  }

  @Override
  public String toString() {
    return "SessionIdentity{" +
      "id='" + id + '\'' +
      ", token='" + token + '\'' +
      '}';
  }
}
