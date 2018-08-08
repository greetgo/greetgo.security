package kz.greetgo.security.session;

import kz.greetgo.security.session.jdbc.SelectDateOrNull;
import kz.greetgo.security.session.jdbc.SelectFirstOrNull;
import kz.greetgo.security.session.jdbc.SelectStrOrNull;
import kz.greetgo.security.session.jdbc.Update;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public abstract class AbstractSessionStorageAdapter implements SessionStorage {

  protected final SessionStorageBuilder.Names names;

  public AbstractSessionStorageAdapter(SessionStorageBuilder.Names names) {
    this.names = names;
    init();
  }

  private void init() {
    try {
      names.jdbc.execute(new SelectStrOrNull(checkTableExistsSql()));
    } catch (RuntimeException e) {
      if (e.getCause() instanceof SQLException) {
        SQLException sqlException = (SQLException) e.getCause();
        if (isExceptionAboutTableDoesNotExists(sqlException)) {
          createSessionTable();
          return;
        }
        throw new RuntimeException("SQL State = " + sqlException.getSQLState(), sqlException);
      }
      throw e;
    }
  }

  private void createSessionTable() {
    names.jdbc.execute(new Update(createSessionTableSql()));
  }

  protected abstract String createSessionTableSql();

  protected abstract boolean isExceptionAboutTableDoesNotExists(SQLException sqlException);

  protected abstract String checkTableExistsSql();

  protected abstract String insertSessionSql(List<Object> sqlParams, SessionIdentity identity, Object sessionData);

  @Override
  public void insertSession(SessionIdentity identity, Object sessionData) {
    List<Object> sqlParams = new ArrayList<>();
    String sql = insertSessionSql(sqlParams, identity, sessionData);
    names.jdbc.execute(new Update(sql, sqlParams));
  }

  @Override
  public boolean zeroSessionAge(String sessionId) {
    List<Object> sqlParams = new ArrayList<>();
    String sql = zeroSessionAgeSql(sqlParams, sessionId);
    return names.jdbc.execute(new Update(sql, sqlParams)) > 0;
  }

  protected abstract String zeroSessionAgeSql(List<Object> sqlParams, String sessionId);

  @Override
  public Date loadLastTouchedAt(String sessionId) {
    List<Object> sqlParams = new ArrayList<>();
    String sql = loadLastTouchedAtSql(sqlParams, sessionId);
    return names.jdbc.execute(new SelectDateOrNull(sql, sqlParams));
  }

  protected abstract String loadLastTouchedAtSql(List<Object> sqlParams, String sessionId);

  @Override
  public int removeSessionsOlderThan(int ageInHours) {
    List<Object> sqlParams = new ArrayList<>();
    String sql = removeSessionsOlderThanSql(sqlParams, ageInHours);
    return names.jdbc.execute(new Update(sql, sqlParams));
  }

  protected abstract String removeSessionsOlderThanSql(List<Object> sqlParams, int ageInHours);

  @Override
  public SessionRow loadSession(String sessionId) {

    String sql = "select * from " + names.tableName + " where " + names.id + " = ?";

    return names.jdbc.execute(new SelectFirstOrNull<>(sql, singletonList(sessionId), rs -> {

      String token = rs.getString(names.token);
      Object sessionData = Serializer.deserialize(rs.getBytes(names.sessionData));
      Date insertedAt = rs.getTimestamp(names.insertedAt);
      Date lastTouchedAt = rs.getTimestamp(names.lastTouchedAt);

      return new SessionRow(token, sessionData, insertedAt, lastTouchedAt);
    }));
  }

  @Override
  public boolean remove(String sessionId) {
    String sql = "delete from " + names.tableName + " where " + names.id + " = ?";
    return names.jdbc.execute(new Update(sql, singletonList(sessionId))) > 0;
  }

  @Override
  public boolean setLastTouchedAt(String sessionId, Date lastTouchedAt) {
    Objects.requireNonNull(lastTouchedAt, "lastTouchedAt cannot be null");
    String sql = ""
      + " update " + names.tableName
      + " set    " + names.lastTouchedAt + " = ?"
      + " where  " + names.id + " = ?";
    return names.jdbc.execute(new Update(
      sql,
      asList(new Timestamp(lastTouchedAt.getTime()), sessionId))
    ) > 0;
  }
}
