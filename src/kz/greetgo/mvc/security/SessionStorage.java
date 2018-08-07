package kz.greetgo.mvc.security;

public interface SessionStorage {
  void setSessionBytes(byte[] bytes);

  byte[] getSessionBytes();

  String viewSessionObject();
}
