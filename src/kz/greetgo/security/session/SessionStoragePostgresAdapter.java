package kz.greetgo.security.session;

import java.sql.SQLException;
import java.util.List;

class SessionStoragePostgresAdapter extends AbstractSessionStorageAdapter implements SessionStorage {
  SessionStoragePostgresAdapter(SessionStorageBuilder.Structure structure) {
    super(structure);
  }

  @Override
  protected String checkTableExistsSql() {
    return "select " + structure.id + " from " + structure.tableName + " limit 1";
  }

  @Override
  protected String insertSessionSql(List<Object> sqlParams, SessionIdentity identity, Object sessionData) {
    sqlParams.add(identity.id);
    sqlParams.add(identity.token);
    sqlParams.add(Serializer.serialize(sessionData));

    return "insert into " + structure.tableName + " (" +
      structure.id +
      ", " +
      structure.token +
      ", " +
      structure.sessionData +
      ") values (?, ?, ?)";
  }

  @Override
  protected String createSessionTableSql() {
    return "create table " + structure.tableName + " (" +
      "  " + structure.id + " varchar(50) not null," +
      "  " + structure.token + " varchar(50)," +
      "  " + structure.sessionData + " byTea," +
      "  " + structure.insertedAt + " timestamp not null default clock_timestamp()," +
      "  " + structure.lastTouchedAt + " timestamp not null default clock_timestamp()," +
      "  primary key(" + structure.id + ")" +
      ")";
  }

  @Override
  protected boolean isExceptionAboutTableDoesNotExists(SQLException sqlException) {
    return "42P01".equals(sqlException.getSQLState());
  }

  @Override
  protected String loadLastTouchedAtSql(List<Object> sqlParams, String sessionId) {
    sqlParams.add(sessionId);
    return "select " + structure.lastTouchedAt + " from " + structure.tableName + " where " + structure.id + " = ?";
  }

  @Override
  protected String zeroSessionAgeSql(List<Object> sqlParams, String sessionId) {
    sqlParams.add(sessionId);
    return "update " + structure.tableName
      + " set " + structure.lastTouchedAt + " = clock_timestamp()" +
      " where " + structure.id + " = ?";
  }

  @Override
  protected String removeSessionsOlderThanSql(List<Object> sqlParams, int ageInHours) {
    return "delete from " + structure.tableName + " where " + structure.lastTouchedAt
      + " < clock_timestamp() - interval '" + ageInHours + " hours'";
  }
}
