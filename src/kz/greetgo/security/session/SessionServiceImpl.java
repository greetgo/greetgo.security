package kz.greetgo.security.session;

import kz.greetgo.security.errors.SerializedClassChanged;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

class SessionServiceImpl implements SessionService {
  private final SessionServiceBuilder builder;

  @SuppressWarnings("SpellCheckingInspection")
  private static final String ENG = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String DEG = "0123456789";
  private static final char[] ALL = (
    ENG.toLowerCase() + ENG.toUpperCase() + DEG
  ).toCharArray();
  private static final int ALL_LEN = ALL.length;
  private final Random random = new SecureRandom();

  private String generateId(int length) {

    int[] randomIndexes = random.ints()
      .limit(length)
      .map(i -> i < 0 ? -i : i)
      .map(i -> i % ALL_LEN)
      .toArray();

    char[] chars = new char[length];

    for (int i = 0; i < length; i++) {
      chars[i] = ALL[randomIndexes[i]];
    }

    return new String(chars);
  }

  public SessionServiceImpl(SessionServiceBuilder builder) {
    this.builder = builder;
  }

  static class SessionCache {
    final Object sessionData;
    final String token;
    final AtomicReference<Date> lastTouchedAt;

    public SessionCache(Object sessionData, String token, Date lastTouchedAt) {
      this.sessionData = sessionData;
      this.token = token;
      this.lastTouchedAt = new AtomicReference<>(lastTouchedAt);
    }
  }

  final ConcurrentMap<String, SessionCache> sessionCacheMap = new ConcurrentHashMap<>();
  final ConcurrentMap<String, String> removedSessionIds = new ConcurrentHashMap<>();

  @Override
  public Map<String, String> statisticsInfo() {
    HashMap<String, String> ret = new HashMap<>();
    ret.put("Sessions cache size", "" + sessionCacheMap.size());
    ret.put("Removed sessions map size", "" + removedSessionIds.size());
    return ret;
  }

  @Override
  public SessionIdentity createSession(Object sessionData) {
    String sessionIdPart = generateId(builder.sessionIdLength);
    String sessionSalt = builder.saltGenerator.generateSalt(sessionIdPart);
    String sessionId = new SessionId(sessionSalt, sessionIdPart).toString();
    String token = generateId(builder.tokenLength);
    SessionIdentity identity = new SessionIdentity(sessionId, token);

    builder.storage.insertSession(identity, sessionData);

    Date lastTouchedAt = loadLastTouchedAt(identity.id);

    SessionCache sessionCache = new SessionCache(sessionData, token, lastTouchedAt);

    sessionCacheMap.put(sessionId, sessionCache);

    return identity;
  }

  //TODO pompei добавить этому методу кэширование
  private Date loadLastTouchedAt(String sessionId) {
    return builder.storage.loadLastTouchedAt(sessionId);
  }

  private static <T> T cast(Object object) {
    //noinspection unchecked
    return (T) object;
  }

  @Override
  public <T> T getSessionData(String sessionId) {

    if (removedSessionIds.containsKey(sessionId)) {
      return null;
    }


    {
      SessionCache sessionCache = sessionCacheMap.get(sessionId);
      if (sessionCache != null) {
        return cast(sessionCache.sessionData);
      }
    }

    return cast(loadSession(sessionId).map(row -> row.sessionData).orElse(null));

  }

  private Optional<SessionRow> loadSession(String sessionId) {

    try {

      SessionRow sessionRow = builder.storage.loadSession(sessionId);
      if (sessionRow == null) {
        return Optional.empty();
      }

      sessionCacheMap.put(sessionId, sessionRow.toCacheRecord());
      return Optional.of(sessionRow);

    } catch (SerializedClassChanged e) {

      return Optional.empty();

    }


  }

  @Override
  public boolean verifyId(String sessionId) {
    if (sessionId == null) {
      return false;
    }

    SessionId s = SessionId.parse(sessionId);

    if (s == null) {
      return false;
    }

    if (s.part == null || s.part.isEmpty()) {
      return false;
    }

    if (s.salt == null || s.salt.isEmpty()) {
      return false;
    }

    String saltExpected = builder.saltGenerator.generateSalt(s.part);

    return saltExpected.equals(s.salt);

  }

  @Override
  public boolean verifyToken(String sessionId, String token) {
    SessionCache cache = sessionCacheMap.get(sessionId);
    if (cache != null) {
      return Objects.equals(cache.token, token);
    }

    return loadSession(sessionId)
      .filter(row -> row.token != null && row.token.equals(token))
      .isPresent();
  }

  @Override
  public void zeroSessionAge(String sessionId) {

    if (!verifyId(sessionId)) {
      return;
    }

    if (zeroSessionAgeInCacheIfExists(sessionId)) {
      return;
    }

    if (!loadSession(sessionId).isPresent()) {
      return;
    }

    zeroSessionAgeInCacheIfExists(sessionId);
  }

  private boolean zeroSessionAgeInCacheIfExists(String sessionId) {
    SessionCache sessionCache = sessionCacheMap.get(sessionId);
    if (sessionCache == null) {
      return false;
    }
    sessionCache.lastTouchedAt.set(new Date());
    return true;
  }

  @Override
  public void removeSession(String sessionId) {
    if (!verifyId(sessionId)) return;
    sessionCacheMap.remove(sessionId);
    builder.storage.remove(sessionId);
    removedSessionIds.put(sessionId, sessionId);
  }

  @Override
  public void removeOldSessions() {

    builder.storage.removeSessionsOlderThan(builder.oldSessionAgeInHours);

    Calendar calendar = new GregorianCalendar();
    calendar.add(Calendar.HOUR, -builder.oldSessionAgeInHours);

    Set<String> removingIds = sessionCacheMap.entrySet().stream()
      .filter(s -> s.getValue().lastTouchedAt.get().before(calendar.getTime()))
      .map(Map.Entry::getKey)
      .collect(Collectors.toSet());

    removingIds.forEach(sessionCacheMap::remove);
    removingIds.forEach(id -> removedSessionIds.put(id, id));
  }

  @Override
  public void syncCache() {

    Set<String> removingIds = new HashSet<>();

    for (Map.Entry<String, SessionCache> e : sessionCacheMap.entrySet()) {
      SessionRow sessionRow = builder.storage.loadSession(e.getKey());
      if (sessionRow == null) {
        removingIds.add(e.getKey());
        continue;
      }

      if (sessionRow.lastTouchedAt == null || sessionRow.lastTouchedAt.before(e.getValue().lastTouchedAt.get())) {

        builder.storage.setLastTouchedAt(e.getKey(), e.getValue().lastTouchedAt.get());

      } else {

        e.getValue().lastTouchedAt.set(sessionRow.lastTouchedAt);

      }
    }

    removingIds.forEach(sessionCacheMap::remove);
    removingIds.forEach(id -> removedSessionIds.put(id, id));
  }
}
