package kz.greetgo.security.errors;

import java.io.InvalidClassException;

public class SerializedClassChanged extends RuntimeException {
  public SerializedClassChanged(InvalidClassException e) {
    super(e.getMessage(), e);
  }
}
