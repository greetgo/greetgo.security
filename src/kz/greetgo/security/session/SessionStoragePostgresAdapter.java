package kz.greetgo.security.session;

import java.sql.SQLException;
import java.util.List;

class SessionStoragePostgresAdapter extends AbstractSessionStorageAdapter implements SessionStorage {
  SessionStoragePostgresAdapter(SessionStorageJdbcBuilder.Names names) {
    super(names);
  }

  @Override
  protected String checkTableExistsSql() {
    return "select " + names.id + " from " + names.tableName + " limit 1";
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
      "  " + names.id + " varchar(50) not null," +
      "  " + names.token + " varchar(50)," +
      "  " + names.sessionData + " byTea," +
      "  " + names.insertedAt + " timestamp not null default clock_timestamp()," +
      "  " + names.lastTouchedAt + " timestamp not null default clock_timestamp()," +
      "  primary key(" + names.id + ")" +
      ")";
  }

  @Override
  protected boolean isExceptionAboutTableDoesNotExists(SQLException sqlException) {
    return "42P01".equals(sqlException.getSQLState());
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
      + " set " + names.lastTouchedAt + " = clock_timestamp()" +
      " where " + names.id + " = ?";
  }

  @Override
  protected String removeSessionsOlderThanSql(List<Object> sqlParams, int ageInHours) {
    return "delete from " + names.tableName + " where " + names.lastTouchedAt
      + " < clock_timestamp() - interval '" + ageInHours + " hours'";
  }
}
