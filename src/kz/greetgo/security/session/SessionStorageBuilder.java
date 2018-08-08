package kz.greetgo.security.session;

import kz.greetgo.db.DbType;
import kz.greetgo.db.Jdbc;

import java.util.Objects;

public class SessionStorageBuilder {
  private DbType dbType;

  static class Names {
    Jdbc jdbc = null;

    String tableName = "session_storage";

    String id = "id";
    String token = "token";
    String sessionData = "session_data";
    String insertedAt = "inserted_at";
    String lastTouchedAt = "last_touched_at";
  }

  final Names names = new Names();

  private SessionStorageBuilder() {}

  public static SessionStorageBuilder newBuilder() {
    return new SessionStorageBuilder();
  }

  public SessionStorageBuilder setJdbc(DbType dbType, Jdbc jdbc) {
    Objects.requireNonNull(dbType);
    Objects.requireNonNull(jdbc);
    this.dbType = dbType;
    this.names.jdbc = jdbc;
    return this;
  }

  public SessionStorage build() {
    if (dbType == DbType.Postgres) {
      return new SessionStoragePostgresAdapter(names);
    }

    if (dbType == DbType.Oracle) {
      return new SessionStorageOracleAdapter(names);
    }

    throw new RuntimeException("Unknown db type = " + dbType);
  }

  public SessionStorageBuilder setTableName(String tableName) {
    this.names.tableName = tableName;
    return this;
  }

  public SessionStorageBuilder setFieldId(String id) {
    this.names.id = id;
    return this;
  }

  public SessionStorageBuilder setFieldToken(String token) {
    this.names.token = token;
    return this;
  }

  public SessionStorageBuilder setFieldSessionData(String sessionData) {
    this.names.sessionData = sessionData;
    return this;
  }

  public SessionStorageBuilder setFieldInsertedAt(String insertedAt) {
    this.names.insertedAt = insertedAt;
    return this;
  }

  public SessionStorageBuilder setFieldLastTouchedAt(String lastTouchedAt) {
    this.names.lastTouchedAt = lastTouchedAt;
    return this;
  }
}
