package kz.greetgo.security.crypto;

public interface ContentAccess {
  byte[] downloadBytes();

  void uploadBytes(byte[] bytes);

  boolean exists();

  default void delete() {
    uploadBytes(null);
  }
}
