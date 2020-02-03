package kz.greetgo.security.errors;

public class SerializedClassChanged extends RuntimeException {
  public SerializedClassChanged(Exception e) {
    super(e.getMessage(), e);
  }
}
