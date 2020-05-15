package kz.greetgo.security.session;

import kz.greetgo.security.crypto.Crypto;
import kz.greetgo.security.util.SessionDot;
import kz.greetgo.security.util.SkipListener;
import kz.greetgo.security.util.TestSessionStorage;
import kz.greetgo.util.RND;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import static kz.greetgo.security.SecurityBuilders.newCryptoBuilder;
import static kz.greetgo.security.SecurityBuilders.newSessionServiceBuilder;
import static org.fest.assertions.api.Assertions.assertThat;

@Listeners(SkipListener.class)
public class SessionServiceTest {

  TestSessionStorage sessionStorage;
  SessionService sessionService;
  SessionServiceImpl impl;
  final SaltGenerator saltGenerator = str -> "S" + str.substring(0, 5) + "S";

  private static final int OLD_SESSION_AGE_IN_HOURS = 7;

  @BeforeMethod
  public void createSessionService() {
    sessionStorage = new TestSessionStorage();

    sessionService = newSessionServiceBuilder()
      .setOldSessionAgeInHours(OLD_SESSION_AGE_IN_HOURS)
      .setSessionIdLength(17)
      .setTokenLength(17)
      .setStorage(sessionStorage)
      .setSaltGenerator(saltGenerator)
      .build();

    impl = (SessionServiceImpl) sessionService;
  }

  @Test
  public void createSession() {
    String sessionData = "SESSION DATA " + RND.str(10);

    //
    //
    SessionIdentity identity = sessionService.createSession(sessionData);
    //
    //

    assertThat(identity).isNotNull();
    assertThat(identity.id).isNotNull();
    assertThat(identity.token).isNotNull();
    assertThat(identity.id).isNotEqualTo(identity.token);

    SessionDot sessionDot = sessionStorage.sessionMap.get(identity.id);
    assertThat(sessionDot).isNotNull();
    assertThat(sessionDot.id).isEqualTo(identity.id);
    assertThat(sessionDot.token).isEqualTo(identity.token);
    assertThat(sessionDot.sessionData).isEqualTo(sessionData);
  }

  @Test
  public void getSessionData() {

    String sessionData = "SESSION DATA " + RND.str(10);
    SessionIdentity identity = sessionService.createSession(sessionData);

    //
    //
    Object actual = sessionService.getSessionData(identity.id);
    //
    //

    assertThat(actual).isEqualTo(sessionData);

    assertThat(impl.sessionCacheMap).containsKey(identity.id);

  }

  @Test
  public void getSessionData_fromStorage() {
    sessionStorage.loadSessionCount = 0;

    String sessionData = "SESSION DATA " + RND.str(10);
    SessionIdentity identity = sessionService.createSession(sessionData);

    impl.sessionCacheMap.clear();

    //
    //
    Object actual = sessionService.getSessionData(identity.id);
    //
    //

    assertThat(actual).isEqualTo(sessionData);

    assertThat(sessionStorage.loadSessionCount).isEqualTo(1);
  }

  @Test
  public void getSessionData_inRemoved() {
    String sessionData = "SESSION DATA " + RND.str(10);

    SessionIdentity identity = sessionService.createSession(sessionData);

    impl.removedSessionIds.put(identity.id, identity.id);

    //
    //
    Object actual = sessionService.getSessionData(identity.id);
    //
    //

    assertThat(actual).isNull();
  }

  @Test
  public void getSessionData_fromCache() {
    String sessionData = "SESSION DATA " + RND.str(10);
    SessionIdentity identity = sessionService.createSession(sessionData);

    impl.sessionCacheMap.clear();

    sessionService.getSessionData(identity.id);

    sessionStorage.loadSessionCount = 0;

    //
    //
    Object actual = sessionService.getSessionData(identity.id);
    //
    //

    assertThat(actual).isEqualTo(sessionData);

    assertThat(sessionStorage.loadSessionCount).isZero();
  }

  @Test
  public void getSessionData_absent() {
    sessionStorage.loadSessionCount = 0;

    //
    //
    Object actual = sessionService.getSessionData(RND.str(10));
    //
    //

    assertThat(actual).isNull();

    assertThat(sessionStorage.loadSessionCount).isEqualTo(1);
    assertThat(impl.sessionCacheMap).isEmpty();
  }

  @Test
  public void verifyId() {
    String id = RND.str(10);
    String salt = saltGenerator.generateSalt(id);

    String sessionId = new SessionId(salt, id).toString();

    //
    //
    boolean verificationResult = sessionService.verifyId(sessionId);
    //
    //

    assertThat(verificationResult).isTrue();
  }

  @Test
  public void verifyId_leftId1() {
    //
    //
    boolean verificationResult = sessionService.verifyId(RND.str(10));
    //
    //

    assertThat(verificationResult).isFalse();
  }

  @Test
  public void verifyId_leftId2() {
    //
    //
    boolean verificationResult = sessionService.verifyId(RND.str(10) + "-");
    //
    //

    assertThat(verificationResult).isFalse();
  }

  @Test
  public void verifyId_leftId3() {
    //
    //
    boolean verificationResult = sessionService.verifyId("-" + RND.str(10));
    //
    //

    assertThat(verificationResult).isFalse();
  }


  @Test
  public void verifyId_null() {
    //
    //
    boolean verificationResult = sessionService.verifyId(null);
    //
    //

    assertThat(verificationResult).isFalse();
  }

  @Test
  public void statisticsInfo() {
    Map<String, String> statisticsInfo = sessionService.statisticsInfo();
    assertThat(statisticsInfo).isNotNull();
  }

  @Test
  public void verifyToken() {
    SessionIdentity identity = sessionService.createSession(null);

    impl.sessionCacheMap.clear();
    sessionStorage.loadSessionCount = 0;

    //
    //
    boolean verificationResult = sessionService.verifyToken(identity.id, identity.token);
    //
    //

    assertThat(verificationResult).isTrue();
    assertThat(sessionStorage.loadSessionCount).isEqualTo(1);

    assertThat(impl.sessionCacheMap).containsKey(identity.id);
  }

  @Test
  public void verifyToken_fromCache() {
    SessionIdentity identity = sessionService.createSession(null);

    assertThat(impl.sessionCacheMap).containsKey(identity.id);
    sessionStorage.loadSessionCount = 0;

    //
    //
    boolean verificationResult = sessionService.verifyToken(identity.id, identity.token);
    //
    //

    assertThat(verificationResult).isTrue();
    assertThat(sessionStorage.loadSessionCount).isZero();

    assertThat(impl.sessionCacheMap).containsKey(identity.id);
  }

  @Test
  public void verifyToken_noSession() {
    SessionIdentity identity = sessionService.createSession(null);

    assertThat(impl.sessionCacheMap).containsKey(identity.id);
    impl.sessionCacheMap.clear();
    sessionStorage.loadSessionCount = 0;

    sessionStorage.sessionMap.clear();

    //
    //
    boolean verificationResult = sessionService.verifyToken(identity.id, identity.token);
    //
    //

    assertThat(verificationResult).isFalse();
    assertThat(sessionStorage.loadSessionCount).isEqualTo(1);

    assertThat(impl.sessionCacheMap).doesNotContainKey(identity.id);
  }

  @Test
  public void verifyToken_anotherToken() {
    SessionIdentity identity1 = sessionService.createSession(null);
    SessionIdentity identity2 = sessionService.createSession(null);

    impl.sessionCacheMap.clear();
    sessionStorage.loadSessionCount = 0;

    //
    //
    boolean verificationResult = sessionService.verifyToken(identity1.id, identity2.token);
    //
    //

    assertThat(verificationResult).isFalse();
    assertThat(sessionStorage.loadSessionCount).isEqualTo(1);

    assertThat(impl.sessionCacheMap).containsKey(identity1.id);
    assertThat(impl.sessionCacheMap).doesNotContainKey(identity2.id);
  }

  @Test
  public void verifyToken_leftToken_noCache() {
    SessionIdentity identity = sessionService.createSession(null);

    impl.sessionCacheMap.clear();
    sessionStorage.loadSessionCount = 0;

    //
    //
    boolean verificationResult = sessionService.verifyToken(identity.id, RND.str(10));
    //
    //

    assertThat(verificationResult).isFalse();
    assertThat(sessionStorage.loadSessionCount).isEqualTo(1);

    assertThat(impl.sessionCacheMap).containsKey(identity.id);
  }

  @Test
  public void verifyToken_leftToken() {
    SessionIdentity identity = sessionService.createSession(null);

    assertThat(impl.sessionCacheMap).containsKey(identity.id);
    sessionStorage.loadSessionCount = 0;

    //
    //
    boolean verificationResult = sessionService.verifyToken(identity.id, RND.str(10));
    //
    //

    assertThat(verificationResult).isFalse();
    assertThat(sessionStorage.loadSessionCount).isZero();

    assertThat(impl.sessionCacheMap).containsKey(identity.id);
  }

  public static Date nowAddHours(int hours) {
    Calendar calendar = new GregorianCalendar();
    calendar.add(Calendar.HOUR, hours);
    return calendar.getTime();
  }

  @Test
  public void zeroSessionAge_usedCache() {
    SessionIdentity identity = sessionService.createSession(null);

    {
      SessionServiceImpl.SessionCache cache = impl.sessionCacheMap.get(identity.id);
      assertThat(cache).isNotNull();
      cache.lastTouchedAt.set(nowAddHours(-10));
    }

    sessionStorage.loadSessionCount = 0;

    //
    //
    sessionService.zeroSessionAge(identity.id);
    //
    //

    assertThat(sessionStorage.loadSessionCount).isZero();

    {
      SessionServiceImpl.SessionCache cache = impl.sessionCacheMap.get(identity.id);
      assertThat(cache).isNotNull();
      assertThat(cache.lastTouchedAt.get()).isAfter(nowAddHours(-1));
    }
  }

  @Test
  public void zeroSessionAge_noCache() {

    SessionIdentity identity = sessionService.createSession(null);

    sessionStorage.sessionMap.get(identity.id).lastTouchedAt = nowAddHours(-10);

    impl.sessionCacheMap.clear();
    sessionStorage.loadSessionCount = 0;

    //
    //
    sessionService.zeroSessionAge(identity.id);
    //
    //

    assertThat(sessionStorage.loadSessionCount).isEqualTo(1);

    {
      SessionServiceImpl.SessionCache cache = impl.sessionCacheMap.get(identity.id);
      assertThat(cache).isNotNull();
      assertThat(cache.lastTouchedAt.get()).isAfter(nowAddHours(-1));
    }
    {
      SessionDot sessionDot = sessionStorage.sessionMap.get(identity.id);
      assertThat(sessionDot).isNotNull();
      assertThat(sessionDot.lastTouchedAt)
        .describedAs("Session must be being pinging only in cache")
        .isBefore(nowAddHours(-1));
    }
  }

  @Test
  public void zeroSessionAge_leftSessionId() {

    String sessionId = RND.str(10);

    sessionStorage.loadSessionCount = 0;

    //
    //
    sessionService.zeroSessionAge(sessionId);
    //
    //

    assertThat(sessionStorage.loadSessionCount).isZero();
  }

  @Test
  public void zeroSessionAge_noSessionInDb() {

    SessionIdentity identity = sessionService.createSession(null);

    sessionStorage.sessionMap.remove(identity.id);

    impl.sessionCacheMap.clear();
    sessionStorage.loadSessionCount = 0;

    //
    //
    sessionService.zeroSessionAge(identity.id);
    //
    //

    assertThat(sessionStorage.loadSessionCount).isEqualTo(1);

    assertThat(impl.sessionCacheMap).doesNotContainKey(identity.id);
  }

  @Test
  public void removeSession() {
    SessionIdentity identity = sessionService.createSession(null);

    assertThat(sessionStorage.sessionMap).containsKey(identity.id);
    assertThat(impl.sessionCacheMap).containsKey(identity.id);

    //
    //
    sessionService.removeSession(identity.id);
    //
    //

    assertThat(sessionStorage.sessionMap).doesNotContainKey(identity.id);
    assertThat(impl.sessionCacheMap).doesNotContainKey(identity.id);
    assertThat(impl.removedSessionIds).containsKey(identity.id);
  }

  @Test
  public void removeSession_noSession_leftId() {
    String sessionId = RND.str(10);

    //
    //
    sessionService.removeSession(sessionId);
    //
    //

    assertThat(impl.removedSessionIds).doesNotContainKey(sessionId);
  }

  @Test
  public void removeSession_noSession_goodId() {
    SessionIdentity identity = sessionService.createSession(null);

    sessionStorage.sessionMap.remove(identity.id);
    impl.sessionCacheMap.remove(identity.id);

    //
    //
    sessionService.removeSession(identity.id);
    //
    //

    assertThat(impl.removedSessionIds).containsKey(identity.id);
  }

  private void setLastTouchedByInDb(String sessionId, Date value) {
    sessionStorage.sessionMap.get(sessionId).lastTouchedAt = value;
    {
      SessionServiceImpl.SessionCache cache = impl.sessionCacheMap.get(sessionId);
      if (cache != null) {
        cache.lastTouchedAt.set(value);
      }
    }
  }

  @Test
  public void removeOldSessions() {
    String youngId = sessionService.createSession(null).id;
    String oldId = sessionService.createSession(null).id;

    setLastTouchedByInDb(youngId, nowAddHours(-OLD_SESSION_AGE_IN_HOURS + 1));
    setLastTouchedByInDb(oldId, nowAddHours(-OLD_SESSION_AGE_IN_HOURS - 1));

    //
    //
    sessionService.removeOldSessions();
    //
    //

    assertThat(sessionStorage.sessionMap).containsKey(youngId);
    assertThat(impl.sessionCacheMap).containsKey(youngId);
    assertThat(impl.removedSessionIds).doesNotContainKey(youngId);

    assertThat(sessionStorage.sessionMap).doesNotContainKey(oldId);
    assertThat(impl.sessionCacheMap).doesNotContainKey(oldId);
    assertThat(impl.removedSessionIds).containsKey(oldId);
  }

  @Test
  public void syncCache_existsInCache_absentInDb() {

    String sessionId = RND.str(10);
    impl.sessionCacheMap.put(sessionId, new SessionServiceImpl.SessionCache(sessionId, RND.str(10), new Date()));

    sessionStorage.loadSessionCount = 0;

    //
    //
    sessionService.syncCache();
    //
    //

    assertThat(impl.sessionCacheMap).doesNotContainKey(sessionId);
    assertThat(impl.removedSessionIds).containsKey(sessionId);

    assertThat(sessionStorage.loadSessionCount).isEqualTo(1);
  }

  @Test
  public void syncCache_cacheYoungerThenDb() {

    String sessionId = sessionService.createSession(null).id;

    impl.sessionCacheMap.get(sessionId).lastTouchedAt.set(nowAddHours(-10));
    sessionStorage.sessionMap.get(sessionId).lastTouchedAt = nowAddHours(-5);

    sessionStorage.loadSessionCount = 0;

    //
    //
    sessionService.syncCache();
    //
    //

    assertThat(impl.sessionCacheMap).containsKey(sessionId);
    assertThat(impl.removedSessionIds).doesNotContainKey(sessionId);

    assertThat(impl.sessionCacheMap.get(sessionId).lastTouchedAt.get()).isAfter(nowAddHours(-7));
    assertThat(sessionStorage.sessionMap.get(sessionId).lastTouchedAt).isAfter(nowAddHours(-7));

    assertThat(sessionStorage.loadSessionCount).isEqualTo(1);
  }

  @Test
  public void syncCache_cacheOlderThenDb() {

    String sessionId = sessionService.createSession(null).id;

    impl.sessionCacheMap.get(sessionId).lastTouchedAt.set(nowAddHours(-5));
    sessionStorage.sessionMap.get(sessionId).lastTouchedAt = nowAddHours(-10);

    sessionStorage.loadSessionCount = 0;

    //
    //
    sessionService.syncCache();
    //
    //

    assertThat(impl.sessionCacheMap).containsKey(sessionId);
    assertThat(impl.removedSessionIds).doesNotContainKey(sessionId);

    assertThat(impl.sessionCacheMap.get(sessionId).lastTouchedAt.get()).isAfter(nowAddHours(-7));
    assertThat(sessionStorage.sessionMap.get(sessionId).lastTouchedAt).isAfter(nowAddHours(-7));

    assertThat(sessionStorage.loadSessionCount).isEqualTo(1);
  }

  @Test
  public void saltGeneratorFromCrypto() {
    String dir = "build/test_data/saltGeneratorFromCrypto/";

    Crypto crypto = newCryptoBuilder()
      .inFiles(new File(dir + "pri.key"), new File(dir + "pub.key"))
      .build();

    SessionService sessionService = newSessionServiceBuilder()
      .setOldSessionAgeInHours(OLD_SESSION_AGE_IN_HOURS)
      .setSessionIdLength(17)
      .setTokenLength(17)
      .setStorage(sessionStorage)
      .setSaltGeneratorOnCrypto(crypto, 17, RND.byteArray(100))
      .build();

    SessionIdentity identity = sessionService.createSession(null);
//    System.out.println(identity);
    assertThat(identity).isNotNull();
  }
}
