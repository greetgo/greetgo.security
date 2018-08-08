package kz.greetgo.security.session;

import java.sql.SQLException;
import java.util.List;

class SessionStorageOracleAdapter extends AbstractSessionStorageAdapter implements SessionStorage {
  SessionStorageOracleAdapter(SessionStorageBuilder.Structure structure) {
    super(structure);
  }

  @Override
  protected String checkTableExistsSql() {
    return "select " + structure.id + " from " + structure.tableName + " where rowNum = 1";
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
      "  " + structure.id + " varchar2(50) not null," +
      "  " + structure.token + " varchar2(50)," +
      "  " + structure.sessionData + " blob," +
      "  " + structure.insertedAt + " timestamp default current_timestamp not null ," +
      "  " + structure.lastTouchedAt + " timestamp default current_timestamp not null," +
      "  primary key(" + structure.id + ")" +
      ")";
  }

  @Override
  protected boolean isExceptionAboutTableDoesNotExists(SQLException sqlException) {
    return sqlException.getMessage().startsWith("ORA-00942:");
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
      + " set " + structure.lastTouchedAt + " = current_timestamp" +
      " where " + structure.id + " = ?";
  }

  @Override
  protected String removeSessionsOlderThanSql(List<Object> sqlParams, int ageInHours) {
    return "delete from " + structure.tableName + " where " + structure.lastTouchedAt
      + " < current_timestamp - interval '" + ageInHours + "' hour";
  }
}
