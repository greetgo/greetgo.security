package kz.greetgo.security.session;

import kz.greetgo.db.DbType;
import kz.greetgo.db.Jdbc;
import kz.greetgo.security.SecurityBuilders;
import kz.greetgo.security.factory.JdbcFactory;
import kz.greetgo.security.jdbc.SelectBytesField;
import kz.greetgo.security.jdbc.SelectDateField;
import kz.greetgo.security.jdbc.SelectNow;
import kz.greetgo.security.jdbc.SelectStrField;
import kz.greetgo.security.session.jdbc.SelectIntOrNull;
import kz.greetgo.security.session.jdbc.Update;
import kz.greetgo.util.RND;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static java.util.Collections.singletonList;
import static org.fest.assertions.api.Assertions.assertThat;

public class SessionStorageTest {
  JdbcFactory jdbcFactory = new JdbcFactory();

  public static Date nowAddHours(Jdbc jdbc, int hours) {
    Calendar calendar = new GregorianCalendar();
    calendar.setTime(jdbc.execute(new SelectNow()));
    calendar.add(Calendar.HOUR, hours);
    return calendar.getTime();
  }

  @BeforeMethod
  public void createJdbcFactory() {
    jdbcFactory.defineDbNameFrom("session_storage");
  }

  @DataProvider
  private Object[][] dbTypeDataProvider() {
    return new Object[][]{
      {DbType.Postgres}, {DbType.Postgres},
      {DbType.Oracle}, {DbType.Oracle},
    };
  }

  public static class TestSessionData implements Serializable {
    public String userId;
    public String role;
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void insertAndGet(DbType dbType) {
    jdbcFactory.dbType = dbType;
    Jdbc jdbc = jdbcFactory.create();

    String tableName = "s_storage_" + RND.intStr(7);
    SessionStorage sessionStorage = SecurityBuilders.newSessionStorageBuilder()
      .setJdbc(dbType, jdbc)
      .setTableName(tableName)
      .setFieldId("id")
      .setFieldToken("unique_token")
      .setFieldSessionData("user_data")
      .setFieldInsertedAt("ins_at")
      .setFieldLastTouchedAt("touched_at")
      .build();


    SessionIdentity identity = new SessionIdentity(RND.intStr(15), RND.str(10));

    TestSessionData sessionData = new TestSessionData();
    sessionData.userId = RND.str(10);
    sessionData.role = RND.str(10);

    //
    //
    sessionStorage.insertSession(identity, sessionData);
    //
    //

    String actualToken = jdbc.execute(new SelectStrField("unique_token", tableName, identity.id));
    assertThat(actualToken).isEqualTo(identity.token);

    assertThat(jdbc.execute(new SelectDateField("ins_at", tableName, identity.id))).isNotNull();
    assertThat(jdbc.execute(new SelectDateField("touched_at", tableName, identity.id))).isNotNull();
    byte[] bytes = jdbc.execute(new SelectBytesField("user_data", tableName, identity.id));
    TestSessionData actualSessionData = Serializer.deserialize(bytes);
    assertThat(actualSessionData).isNotNull();
    assert actualSessionData != null;
    assertThat(actualSessionData.userId).isEqualTo(sessionData.userId);
    assertThat(actualSessionData.role).isEqualTo(sessionData.role);
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void loadSessionData(DbType dbType) {
    jdbcFactory.dbType = dbType;
    Jdbc jdbc = jdbcFactory.create();

    SessionStorage sessionStorage = SecurityBuilders.newSessionStorageBuilder()
      .setJdbc(dbType, jdbc)
      .build();

    SessionIdentity identity = new SessionIdentity(RND.intStr(15), RND.str(10));

    TestSessionData sessionData = new TestSessionData();
    sessionData.userId = RND.str(10);
    sessionData.role = RND.str(10);
    sessionStorage.insertSession(identity, sessionData);

    //
    //
    SessionRow actual = sessionStorage.loadSession(identity.id);
    //
    //

    assert actual != null;
    assertThat(((TestSessionData) actual.sessionData).userId).isEqualTo(sessionData.userId);
    assertThat(((TestSessionData) actual.sessionData).role).isEqualTo(sessionData.role);
    assertThat(actual.token).isEqualTo(identity.token);
    assertThat(actual.insertedAt).isNotNull();
    assertThat(actual.lastTouchedAt).isNotNull();
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void loadSessionData_sessionData_null(DbType dbType) {
    jdbcFactory.dbType = dbType;
    Jdbc jdbc = jdbcFactory.create();

    SessionStorage sessionStorage = SecurityBuilders.newSessionStorageBuilder()
      .setJdbc(dbType, jdbc)
      .build();

    SessionIdentity identity = new SessionIdentity(RND.intStr(15), RND.str(10));

    sessionStorage.insertSession(identity, null);

    //
    //
    SessionRow row = sessionStorage.loadSession(identity.id);
    //
    //

    assertThat(row.sessionData).isNull();
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void loadSessionData_token_null(DbType dbType) {
    jdbcFactory.dbType = dbType;
    Jdbc jdbc = jdbcFactory.create();

    SessionStorage sessionStorage = SecurityBuilders.newSessionStorageBuilder()
      .setJdbc(dbType, jdbc)
      .build();

    SessionIdentity identity = new SessionIdentity(RND.intStr(15), null);

    sessionStorage.insertSession(identity, null);

    //
    //
    SessionRow row = sessionStorage.loadSession(identity.id);
    //
    //

    assertThat(row.token).isNull();
  }

  @SuppressWarnings("SameParameterValue")
  private void setDateFieldInAllTable(Jdbc jdbc, String fieldName, Date time) {
    jdbc.execute(new Update("update session_storage set " + fieldName + " = ?",
      singletonList(new Timestamp(time.getTime()))));
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void loadLastTouchedAt(DbType dbType) {
    jdbcFactory.dbType = dbType;
    Jdbc jdbc = jdbcFactory.create();

    SessionStorage sessionStorage = SecurityBuilders.newSessionStorageBuilder()
      .setJdbc(dbType, jdbc)
      .build();

    SessionIdentity identity = new SessionIdentity(RND.intStr(15), null);

    sessionStorage.insertSession(identity, null);

    setDateFieldInAllTable(jdbc, "last_touched_at", nowAddHours(jdbc, -5));

    //
    //
    Date lastTouchedAt = sessionStorage.loadLastTouchedAt(identity.id);
    //
    //

    assertThat(lastTouchedAt).isNotNull();

    assertThat(lastTouchedAt).isBefore(nowAddHours(jdbc, -4));

  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void zeroSessionAge(DbType dbType) {
    jdbcFactory.dbType = dbType;
    Jdbc jdbc = jdbcFactory.create();

    SessionStorage sessionStorage = SecurityBuilders.newSessionStorageBuilder()
      .setJdbc(dbType, jdbc)
      .build();

    SessionIdentity identity = new SessionIdentity(RND.intStr(15), null);

    sessionStorage.insertSession(identity, null);

    setDateFieldInAllTable(jdbc, "last_touched_at", nowAddHours(jdbc, -5));

    //
    //
    boolean updated = sessionStorage.zeroSessionAge(identity.id);
    //
    //

    assertThat(updated).isTrue();

    Date actual = jdbc.execute(new SelectDateField("last_touched_at", "session_storage", identity.id));
    assertThat(actual).isAfter(nowAddHours(jdbc, -1));
  }

  private SessionIdentity insertSession(SessionStorage sessionStorage) {
    SessionIdentity identity = new SessionIdentity(RND.intStr(15), null);
    sessionStorage.insertSession(identity, null);
    return identity;
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void zeroSessionAge_leftSessionId(DbType dbType) {
    jdbcFactory.dbType = dbType;
    Jdbc jdbc = jdbcFactory.create();

    SessionStorage sessionStorage = SecurityBuilders.newSessionStorageBuilder()
      .setJdbc(dbType, jdbc)
      .build();

    //
    //
    boolean updated = sessionStorage.zeroSessionAge(RND.str(10));
    //
    //

    assertThat(updated).isFalse();
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void removeSessionsOlderThan(DbType dbType) {
    jdbcFactory.dbType = dbType;
    Jdbc jdbc = jdbcFactory.create();

    SessionStorage sessionStorage = SecurityBuilders.newSessionStorageBuilder()
      .setJdbc(dbType, jdbc)
      .build();

    jdbc.execute(new Update("delete from session_storage"));

    insertSession(sessionStorage);
    insertSession(sessionStorage);
    insertSession(sessionStorage);

    setDateFieldInAllTable(jdbc, "last_touched_at", nowAddHours(jdbc, -10));

    insertSession(sessionStorage);
    insertSession(sessionStorage);

    //
    //
    int count = sessionStorage.removeSessionsOlderThan(7);
    //
    //

    assertThat(count).isEqualTo(3);

    int leaveCount = jdbc.execute(new SelectIntOrNull("select count(1) from session_storage"));
    assertThat(leaveCount).isEqualTo(2);
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void removeSession(DbType dbType) {
    jdbcFactory.dbType = dbType;
    Jdbc jdbc = jdbcFactory.create();

    SessionStorage sessionStorage = SecurityBuilders.newSessionStorageBuilder()
      .setJdbc(dbType, jdbc)
      .build();

    String sessionId = insertSession(sessionStorage).id;

    //
    //
    boolean removeFlag = sessionStorage.remove(sessionId);
    //
    //

    assertThat(removeFlag).isTrue();

    int count = jdbc.execute(new SelectIntOrNull(
      "select count(1) from session_storage where id = ?", singletonList(sessionId)
    ));
    assertThat(count).isZero();
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void removeSession_noSession(DbType dbType) {
    jdbcFactory.dbType = dbType;
    Jdbc jdbc = jdbcFactory.create();

    SessionStorage sessionStorage = SecurityBuilders.newSessionStorageBuilder()
      .setJdbc(dbType, jdbc)
      .build();

    //
    //
    boolean removeFlag = sessionStorage.remove(RND.str(10));
    //
    //

    assertThat(removeFlag).isFalse();
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void setLastTouchedAt(DbType dbType) {
    jdbcFactory.dbType = dbType;
    Jdbc jdbc = jdbcFactory.create();

    SessionStorage sessionStorage = SecurityBuilders.newSessionStorageBuilder()
      .setJdbc(dbType, jdbc)
      .build();

    String sessionId = insertSession(sessionStorage).id;

    setDateFieldInAllTable(jdbc, "last_touched_at", nowAddHours(jdbc, -10));

    //
    //
    boolean updateStatus = sessionStorage.setLastTouchedAt(sessionId, nowAddHours(jdbc, -5));
    //
    //

    assertThat(updateStatus).isTrue();

    Date actualDate = jdbc.execute(new SelectDateField("last_touched_at", "session_storage", sessionId));
    assertThat(actualDate).isAfter(nowAddHours(jdbc, -7));
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void setLastTouchedAt_noSession(DbType dbType) {
    jdbcFactory.dbType = dbType;
    Jdbc jdbc = jdbcFactory.create();

    SessionStorage sessionStorage = SecurityBuilders.newSessionStorageBuilder()
      .setJdbc(dbType, jdbc)
      .build();

    String sessionId = insertSession(sessionStorage).id;

    setDateFieldInAllTable(jdbc, "last_touched_at", nowAddHours(jdbc, -10));

    //
    //
    boolean updateStatus = sessionStorage.setLastTouchedAt(RND.str(10), nowAddHours(jdbc, -5));
    //
    //

    assertThat(updateStatus).isFalse();

    Date actualDate = jdbc.execute(new SelectDateField("last_touched_at", "session_storage", sessionId));
    assertThat(actualDate).isBefore(nowAddHours(jdbc, -7));
  }

}
