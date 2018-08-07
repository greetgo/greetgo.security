package kz.greetgo.security.session;

public class SessionBuilders {
  public static SessionServiceBuilder newServiceBuilder() {
    return new SessionServiceBuilder();
  }

  public static SessionStorageBuilder newStorageBuilder() {
    return new SessionStorageBuilder();
  }
}
