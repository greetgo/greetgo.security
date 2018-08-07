package ka.greetgo.db.session;

import kz.greetgo.db.DbType;
import kz.greetgo.db.Jdbc;

import java.util.Objects;

class SessionStorageBuilder {
  private DbType dbType;

  static class Structure {
    Jdbc jdbc = null;

    String tableName = "session_storage";

    String id = "id";
    String token = "token";
    String sessionData = "session_data";
    String insertedAt = "inserted_at";
    String lastTouchedAt = "last_touched_at";
  }

  final Structure structure = new Structure();


  public SessionStorageBuilder setJdbc(DbType dbType, Jdbc jdbc) {
    Objects.requireNonNull(dbType);
    Objects.requireNonNull(jdbc);
    this.dbType = dbType;
    this.structure.jdbc = jdbc;
    return this;
  }

  public SessionStorage build() {
    if (dbType == DbType.Postgres) {
      return new SessionStoragePostgresAdapter(structure);
    }

    throw new RuntimeException("Unknown db type = " + dbType);
  }

  public SessionStorageBuilder setTableName(String tableName) {
    this.structure.tableName = tableName;
    return this;
  }

  public SessionStorageBuilder setFieldId(String id) {
    this.structure.id = id;
    return this;
  }

  public SessionStorageBuilder setFieldToken(String token) {
    this.structure.token = token;
    return this;
  }

  public SessionStorageBuilder setFieldSessionData(String sessionData) {
    this.structure.sessionData = sessionData;
    return this;
  }

  public SessionStorageBuilder setFieldInsertedAt(String insertedAt) {
    this.structure.insertedAt = insertedAt;
    return this;
  }

  public SessionStorageBuilder setFieldLastTouchedAt(String lastTouchedAt) {
    this.structure.lastTouchedAt = lastTouchedAt;
    return this;
  }
}
