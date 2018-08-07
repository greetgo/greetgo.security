package ka.greetgo.db.session;

public class SessionBuilders {
  public static SessionServiceBuilder newServiceBuilder() {
    return new SessionServiceBuilder();
  }

  public static SessionStorageBuilder newStorageBuilder() {
    return new SessionStorageBuilder();
  }
}
