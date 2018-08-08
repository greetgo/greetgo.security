package kz.greetgo.security.crypto.errors;

import kz.greetgo.db.DbType;

public class UnsupportedDb extends RuntimeException {
  public UnsupportedDb(DbType dbType) {
    super("db type = " + dbType);
  }
}
