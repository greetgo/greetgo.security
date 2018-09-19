package kz.greetgo.security.session;

import java.sql.SQLException;
import java.util.List;

class SessionStorageOracleAdapter extends SessionStorageAdapterAbstract implements SessionStorage {
  SessionStorageOracleAdapter(SessionStorageJdbcBuilder.Names names) {
    super(names);
  }

  @Override
  protected String checkTableExistsSql() {
    return "select " + names.id + " from " + names.tableName + " where rowNum = 1";
  }

  @Override
  protected String insertSessionSql(List<Object> sqlParams, SessionIdentity identity, Object sessionData) {
    sqlParams.add(identity.id);
    sqlParams.add(identity.token);
    sqlParams.add(Serializer.serialize(sessionData));

    return "insert into " + names.tableName + " (" +
      names.id +
      ", " +
      names.token +
      ", " +
      names.sessionData +
      ") values (?, ?, ?)";
  }

  @Override
  protected String createSessionTableSql() {
    return "create table " + names.tableName + " (" +
      "  " + names.id + " varchar2(50) not null," +
      "  " + names.token + " varchar2(50)," +
      "  " + names.sessionData + " blob," +
      "  " + names.insertedAt + " timestamp default current_timestamp not null ," +
      "  " + names.lastTouchedAt + " timestamp default current_timestamp not null," +
      "  primary key(" + names.id + ")" +
      ")";
  }

  @Override
  protected boolean isExceptionAboutTableDoesNotExists(SQLException sqlException) {
    return sqlException.getMessage().startsWith("ORA-00942:");
  }

  @Override
  protected String loadLastTouchedAtSql(List<Object> sqlParams, String sessionId) {
    sqlParams.add(sessionId);
    return "select " + names.lastTouchedAt + " from " + names.tableName + " where " + names.id + " = ?";
  }

  @Override
  protected String zeroSessionAgeSql(List<Object> sqlParams, String sessionId) {
    sqlParams.add(sessionId);
    return "update " + names.tableName
      + " set " + names.lastTouchedAt + " = current_timestamp" +
      " where " + names.id + " = ?";
  }

  @Override
  protected String removeSessionsOlderThanSql(List<Object> sqlParams, int ageInHours) {
    return "delete from " + names.tableName + " where " + names.lastTouchedAt
      + " < current_timestamp - interval '" + ageInHours + "' hour";
  }
}
