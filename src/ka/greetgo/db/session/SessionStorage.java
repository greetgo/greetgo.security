package ka.greetgo.db.session;


import java.util.Date;

/**
 * Stores sessions in some storage
 */
public interface SessionStorage {
  /**
   * Insert new session
   *
   * @param identity    session identity. Unique must be identity.id - it defines session
   * @param sessionData additional session data (to store userId, role or something else)
   */
  void insertSession(SessionIdentity identity, Object sessionData);

  /**
   * Loads sessionContents
   *
   * @param sessionId session id
   * @return session contents
   */
  SessionRow loadSession(String sessionId);

  /**
   * Loads session last touched time
   *
   * @param sessionId session id
   * @return session last touched time or null, if session is absent
   */
  Date loadLastTouchedAt(String sessionId);

  /**
   * Makes session young
   *
   * @param sessionId session id
   * @return indicates session exists:
   * <code>true</code> - session exists and its age made zero,
   * <code>false</code> - session is absent
   */
  boolean zeroSessionAge(String sessionId);

  /**
   * Removes all session which age is more then ageInHours
   *
   * @param ageInHours session age in hours
   * @return count of removed sessions
   */
  int removeSessionsOlderThan(int ageInHours);

  /**
   * Removes session with specified id
   *
   * @param sessionId removing session id
   * @return removing flag: true - session was and removed, false - session absent with specified id
   */
  boolean remove(String sessionId);

  /**
   * Updates field lastTouchedAt of session
   *
   * @param sessionId     updating session id
   * @param lastTouchedAt new value
   * @return update flag: true - was session with specified id and it was update, false - otherwise
   */
  boolean setLastTouchedAt(String sessionId, Date lastTouchedAt);
}
