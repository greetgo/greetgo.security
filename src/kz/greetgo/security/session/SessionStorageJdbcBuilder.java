package kz.greetgo.security.session;

import kz.greetgo.db.DbType;
import kz.greetgo.db.Jdbc;

import java.util.Objects;

public class SessionStorageJdbcBuilder {
  private final DbType dbType;

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

  SessionStorageJdbcBuilder(DbType dbType, Jdbc jdbc) {
    Objects.requireNonNull(dbType);
    Objects.requireNonNull(jdbc);
    this.dbType = dbType;
    this.names.jdbc = jdbc;
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

  public SessionStorageJdbcBuilder setTableName(String tableName) {
    this.names.tableName = tableName;
    return this;
  }

  public SessionStorageJdbcBuilder setFieldId(String id) {
    this.names.id = id;
    return this;
  }

  public SessionStorageJdbcBuilder setFieldToken(String token) {
    this.names.token = token;
    return this;
  }

  public SessionStorageJdbcBuilder setFieldSessionData(String sessionData) {
    this.names.sessionData = sessionData;
    return this;
  }

  public SessionStorageJdbcBuilder setFieldInsertedAt(String insertedAt) {
    this.names.insertedAt = insertedAt;
    return this;
  }

  public SessionStorageJdbcBuilder setFieldLastTouchedAt(String lastTouchedAt) {
    this.names.lastTouchedAt = lastTouchedAt;
    return this;
  }
}
