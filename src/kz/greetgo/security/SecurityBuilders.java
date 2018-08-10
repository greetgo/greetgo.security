package kz.greetgo.security;

import kz.greetgo.security.crypto.CryptoBuilder;
import kz.greetgo.security.password.PasswordEncoderBuilder;
import kz.greetgo.security.session.SessionServiceBuilder;
import kz.greetgo.security.session.SessionStorageBuilder;

public class SecurityBuilders {
  public static SessionServiceBuilder newSessionServiceBuilder() {
    return SessionServiceBuilder.newBuilder();
  }

  public static SessionStorageBuilder newSessionStorageBuilder() {
    return SessionStorageBuilder.newBuilder();
  }

  public static CryptoBuilder newCryptoBuilder() {
    return CryptoBuilder.newBuilder();
  }

  public static PasswordEncoderBuilder newPasswordEncoderBuilder() {
    return PasswordEncoderBuilder.newBuilder();
  }
}
