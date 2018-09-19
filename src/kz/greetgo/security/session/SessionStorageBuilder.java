package kz.greetgo.security.session;

import com.mongodb.DBCollection;
import kz.greetgo.db.DbType;
import kz.greetgo.db.Jdbc;

public class SessionStorageBuilder {

  private SessionStorageBuilder() {}

  public static SessionStorageBuilder newBuilder() {
    return new SessionStorageBuilder();
  }

  public SessionStorageJdbcBuilder setJdbc(DbType dbType, Jdbc jdbc) {
    return new SessionStorageJdbcBuilder(dbType, jdbc);
  }

  public SessionStorageMongoBuilder setMongoCollection(DBCollection collection) {
    return new SessionStorageMongoBuilder(collection);
  }
}
