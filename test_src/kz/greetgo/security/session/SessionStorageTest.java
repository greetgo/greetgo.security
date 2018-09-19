package kz.greetgo.security.session;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import kz.greetgo.db.DbType;
import kz.greetgo.db.Jdbc;
import kz.greetgo.security.SecurityBuilders;
import kz.greetgo.security.factory.JdbcFactory;
import kz.greetgo.security.factory.OracleFactory;
import kz.greetgo.security.jdbc.SelectBytesField;
import kz.greetgo.security.jdbc.SelectDateField;
import kz.greetgo.security.jdbc.SelectNow;
import kz.greetgo.security.jdbc.SelectStrField;
import kz.greetgo.security.session.jdbc.Update;
import kz.greetgo.security.util.SkipListener;
import kz.greetgo.util.RND;
import org.bson.Document;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.fest.assertions.api.Assertions.assertThat;

@Listeners(SkipListener.class)
public class SessionStorageTest {
  JdbcFactory jdbcFactory = new JdbcFactory();

  public static Date nowAddHours(Jdbc jdbc, int hours) {
    Calendar calendar = new GregorianCalendar();
    calendar.setTime(jdbc.execute(new SelectNow()));
    calendar.add(Calendar.HOUR, hours);
    return calendar.getTime();
  }

  public static Date nowAddHours(int hours) {
    Calendar calendar = new GregorianCalendar();
    calendar.add(Calendar.HOUR, hours);
    return calendar.getTime();
  }

  @BeforeMethod
  public void createJdbcFactory() {
    jdbcFactory.defineDbNameFrom("greetgo_security");
  }

  @DataProvider
  private Object[][] dbTypeDataProvider() {

    List<Object[]> list = new ArrayList<>();

    list.add(new Object[]{DbType.Postgres});
    list.add(new Object[]{DbType.Postgres});

    if (OracleFactory.hasOracleDriver()) {
      list.add(new Object[]{DbType.Oracle});
      list.add(new Object[]{DbType.Oracle});
    }

    return list.toArray(new Object[list.size()][]);
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

  @DataProvider
  public Object[][] sessionStorageDataProvider() {
    List<Object[]> list = new ArrayList<>();

    jdbcFactory.defineDbNameFrom("greetgo_security");

    addJdbcBuilder(list, DbType.Postgres);
    addJdbcBuilder(list, DbType.Postgres);

    if (OracleFactory.hasOracleDriver()) {
      addJdbcBuilder(list, DbType.Oracle);
      addJdbcBuilder(list, DbType.Oracle);
    }

    addMongoBuilder(list);
    addMongoBuilder(list);

    return list.toArray(new Object[list.size()][]);
  }

  private void addMongoBuilder(List<Object[]> list) {
    MongoClient mongoClient = new MongoClient();
    MongoDatabase database = mongoClient.getDatabase(System.getProperty("user.name") + "_greetgo_security");
    MongoCollection<Document> sessionStorage = database.getCollection("session_storage");

    list.add(new Object[]{
      SecurityBuilders.newSessionStorageBuilder()
        .setMongoCollection(sessionStorage)
        .build()
    });
  }

  private void addJdbcBuilder(List<Object[]> list, DbType dbType) {
    jdbcFactory.dbType = dbType;
    Jdbc jdbc = jdbcFactory.create();
    list.add(new Object[]{
      SecurityBuilders.newSessionStorageBuilder()
        .setJdbc(jdbcFactory.dbType, jdbc)
        .build()
    });
  }

  @Test(dataProvider = "sessionStorageDataProvider")
  public void insertSession_loadSessionData(SessionStorage sessionStorage) {

    SessionIdentity identity = new SessionIdentity(RND.intStr(15), RND.str(10));

    TestSessionData sessionData = new TestSessionData();
    sessionData.userId = RND.str(10);
    sessionData.role = RND.str(10);

    //
    //
    sessionStorage.insertSession(identity, sessionData);
    //
    //

    //
    //
    SessionRow actual = sessionStorage.loadSession(identity.id);
    //
    //

    assertThat(actual).isNotNull();
    assert actual != null;
    assertThat(((TestSessionData) actual.sessionData).userId).isEqualTo(sessionData.userId);
    assertThat(((TestSessionData) actual.sessionData).role).isEqualTo(sessionData.role);
    assertThat(actual.token).isEqualTo(identity.token);
    assertThat(actual.insertedAt).isNotNull();
    assertThat(actual.lastTouchedAt).isNotNull();
  }

  @Test(dataProvider = "sessionStorageDataProvider")
  public void loadSessionData_sessionData_null(SessionStorage sessionStorage) {

    SessionIdentity identity = new SessionIdentity(RND.intStr(15), RND.str(10));

    sessionStorage.insertSession(identity, null);

    //
    //
    SessionRow row = sessionStorage.loadSession(identity.id);
    //
    //

    assertThat(row).isNotNull();
    assertThat(row.sessionData).isNull();
  }

  @Test(dataProvider = "sessionStorageDataProvider")
  public void loadSessionData_token_null(SessionStorage sessionStorage) {

    SessionIdentity identity = new SessionIdentity(RND.intStr(15), null);

    sessionStorage.insertSession(identity, null);

    //
    //
    SessionRow row = sessionStorage.loadSession(identity.id);
    //
    //

    assertThat(row).isNotNull();
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

  @Test(dataProvider = "sessionStorageDataProvider")
  public void loadLastTouchedAt_notNull(SessionStorage sessionStorage) {

    SessionIdentity identity = new SessionIdentity(RND.intStr(15), null);

    sessionStorage.insertSession(identity, null);

    //
    //
    Date lastTouchedAt = sessionStorage.loadLastTouchedAt(identity.id);
    //
    //

    assertThat(lastTouchedAt).isNotNull();
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

  @Test(dataProvider = "sessionStorageDataProvider")
  public void zeroSessionAge_all(SessionStorage sessionStorage) {

    SessionIdentity identity = new SessionIdentity(RND.intStr(15), null);

    sessionStorage.insertSession(identity, null);

    sessionStorage.setLastTouchedAt(identity.id, nowAddHours(-5));

    //
    //
    boolean updated = sessionStorage.zeroSessionAge(identity.id);
    //
    //

    assertThat(updated).isTrue();

    Date actual = sessionStorage.loadLastTouchedAt(identity.id);

    assertThat(actual).isAfter(nowAddHours(-1));
  }

  private SessionIdentity insertSession(SessionStorage sessionStorage) {
    return insertSession(sessionStorage, null);
  }

  private SessionIdentity insertSession(SessionStorage sessionStorage, Object sessionData) {
    SessionIdentity identity = new SessionIdentity(RND.intStr(15), null);
    sessionStorage.insertSession(identity, sessionData);
    return identity;
  }

  @Test(dataProvider = "sessionStorageDataProvider")
  public void zeroSessionAge_leftSessionId(SessionStorage sessionStorage) {

    //
    //
    boolean updated = sessionStorage.zeroSessionAge(RND.str(10));
    //
    //

    assertThat(updated).isFalse();
  }

  @Test(dataProvider = "sessionStorageDataProvider")
  public void removeSessionsOlderThan(SessionStorage sessionStorage) {

    sessionStorage.removeSessionsOlderThan(7);

    SessionIdentity s1 = insertSession(sessionStorage, "session 1");
    SessionIdentity s2 = insertSession(sessionStorage, "session 2");
    SessionIdentity s3 = insertSession(sessionStorage, "session 3");

    sessionStorage.setLastTouchedAt(s1.id, nowAddHours(-10));
    sessionStorage.setLastTouchedAt(s2.id, nowAddHours(-10));
    sessionStorage.setLastTouchedAt(s3.id, nowAddHours(-10));

    SessionIdentity s4 = insertSession(sessionStorage, "session 4");
    SessionIdentity s5 = insertSession(sessionStorage, "session 5");

    //
    //
    int count = sessionStorage.removeSessionsOlderThan(7);
    //
    //

    assertThat(count).isEqualTo(3);

    assertThat(sessionStorage.loadSession(s1.id)).isNull();
    assertThat(sessionStorage.loadSession(s2.id)).isNull();
    assertThat(sessionStorage.loadSession(s3.id)).isNull();

    assertThat(sessionStorage.loadSession(s4.id).sessionData).isEqualTo("session 4");
    assertThat(sessionStorage.loadSession(s5.id).sessionData).isEqualTo("session 5");
  }

  @Test(dataProvider = "sessionStorageDataProvider")
  public void removeSession(SessionStorage sessionStorage) {

    String sessionId = insertSession(sessionStorage).id;

    assertThat(sessionStorage.loadSession(sessionId)).isNotNull();

    //
    //
    boolean removeFlag = sessionStorage.remove(sessionId);
    //
    //

    assertThat(removeFlag).isTrue();

    assertThat(sessionStorage.loadSession(sessionId)).isNull();

    //
    //
    boolean removeFlag2 = sessionStorage.remove(sessionId);
    //
    //

    assertThat(removeFlag2).isFalse();
  }

  @Test(dataProvider = "sessionStorageDataProvider")
  public void removeSession_noSession(SessionStorage sessionStorage) {

    //
    //
    boolean removeFlag = sessionStorage.remove(RND.str(10));
    //
    //

    assertThat(removeFlag).isFalse();
  }

  @Test(dataProvider = "sessionStorageDataProvider")
  public void setLastTouchedAt(SessionStorage sessionStorage) {

    String sessionId = insertSession(sessionStorage).id;

    //
    //
    boolean updateStatus = sessionStorage.setLastTouchedAt(sessionId, nowAddHours(-5));
    //
    //

    assertThat(updateStatus).isTrue();

    assertThat(sessionStorage.loadSession(sessionId).lastTouchedAt).isBefore(nowAddHours(-4));
  }

  @Test(dataProvider = "sessionStorageDataProvider")
  public void setLastTouchedAt_noSession(SessionStorage sessionStorage) {

    String sessionId = insertSession(sessionStorage).id;

    sessionStorage.setLastTouchedAt(sessionId, nowAddHours(-10));

    //
    //
    boolean updateStatus = sessionStorage.setLastTouchedAt(RND.str(10), nowAddHours(-5));
    //
    //

    assertThat(updateStatus).isFalse();

    assertThat(sessionStorage.loadSession(sessionId)).isNotNull();
    assertThat(sessionStorage.loadSession(sessionId).lastTouchedAt).isBefore(nowAddHours(-7));
  }
}
