package kz.greetgo.security.crypto;

public interface CryptoTrace {
  void trace(Object message);

  void trace(Object message, Throwable error);
}
