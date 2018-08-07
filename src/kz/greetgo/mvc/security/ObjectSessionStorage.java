package kz.greetgo.mvc.security;

import kz.greetgo.mvc.util.Base64Util;

import static kz.greetgo.mvc.security.SerializeUtil.deserialize;
import static kz.greetgo.mvc.security.SerializeUtil.serialize;

public class ObjectSessionStorage implements SessionStorage {

  private static final ThreadLocal<Object> storage = new ThreadLocal<>();

  @Override
  public void setSessionBytes(byte[] bytes) {
    setObject(deserialize(bytes));
  }

  @Override
  public byte[] getSessionBytes() {
    return serialize(getObject());
  }

  @Override
  public String viewSessionObject() {
    return "[[" + getObject() + "]], session bytes = " + Base64Util.bytesToBase64(getSessionBytes());
  }

  protected Object getObject() {
    return storage.get();
  }

  protected void setObject(Object object) {
    storage.set(object);
  }
}
