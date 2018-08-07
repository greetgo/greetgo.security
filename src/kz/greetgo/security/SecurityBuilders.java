package kz.greetgo.security;

import kz.greetgo.security.session.SessionServiceBuilder;
import kz.greetgo.security.session.SessionStorageBuilder;

public class SecurityBuilders {
  public static SessionServiceBuilder newSessionServiceBuilder() {
    return SessionServiceBuilder.newBuilder();
  }

  public static SessionStorageBuilder newSessionStorageBuilder() {
    return SessionStorageBuilder.newBuilder();
  }
}
