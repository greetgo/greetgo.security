package ka.greetgo.db.session;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestSessionStorage implements SessionStorage {

  public final Map<String, SessionDot> sessionMap = new HashMap<>();

  @Override
  public void insertSession(SessionIdentity identity, Object sessionData) {
    SessionDot dot = new SessionDot(identity.id);
    dot.token = identity.token;
    dot.sessionData = sessionData;
    if (sessionMap.containsKey(dot.id)) {
      throw new RuntimeException("Session already exists");
    }
    sessionMap.put(dot.id, dot);
  }

  public int loadSessionCount = 0;

  @Override
  public SessionRow loadSession(String sessionId) {
    loadSessionCount++;
    SessionDot dot = sessionMap.get(sessionId);
    if (dot == null) return null;
    return dot.toRow();
  }

  @Override
  public Date loadLastTouchedAt(String sessionId) {
    SessionDot dot = sessionMap.get(sessionId);
    if (dot == null) return null;
    return dot.lastTouchedAt;
  }


  @Override
  public boolean zeroSessionAge(String sessionId) {
    SessionDot dot = sessionMap.get(sessionId);
    if (dot == null) return false;
    dot.lastTouchedAt = new Date();
    return true;
  }

  @Override
  public boolean setLastTouchedAt(String sessionId, Date value) {
    SessionDot dot = sessionMap.get(sessionId);
    if (dot == null) return false;
    dot.lastTouchedAt = value;
    return true;
  }

  @Override
  public int removeSessionsOlderThan(int ageInHours) {
    Calendar calendar = new GregorianCalendar();
    calendar.add(Calendar.HOUR, -ageInHours);
    List<String> removingIds = sessionMap.values().stream()
      .filter(dot -> dot.lastTouchedAt.before(calendar.getTime()))
      .map(dot -> dot.id)
      .collect(Collectors.toList());

    removingIds.forEach(sessionMap::remove);

    return removingIds.size();
  }

  @Override
  public boolean remove(String sessionId) {
    boolean containsKey = sessionMap.containsKey(sessionId);
    sessionMap.remove(sessionId);
    return containsKey;
  }
}
