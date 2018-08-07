package kz.greetgo.security.session;

import java.util.Map;

/**
 * Generates, caches, stores, give access and removes sessions
 */
public interface SessionService {

  /**
   * Creates new session in storage with specified sessionData
   *
   * @param sessionData additional session data to store in session (it can contain userId, role or else)
   * @return session identity witch contains session id and token. Session id contains salt that nobody cannot create it
   */
  SessionIdentity createSession(Object sessionData);

  /**
   * Gives session data by session id
   *
   * @param sessionId session id
   * @return session data or null if session with specified id is absent
   */
  Object getSessionData(String sessionId);

  /**
   * Verifies session id salt. Very quick operation
   *
   * @param sessionId session id
   * @return verification result: true - session id salt is good, false - otherwise
   */
  boolean verifyId(String sessionId);

  /**
   * Verify token: loads token from storage ot cache and check its identity
   *
   * @param sessionId session id
   * @param token     verifying token
   * @return verification result: true - tokens are same to each other, false - tokens are not same
   */
  boolean verifyToken(String sessionId, String token);

  /**
   * Makes session age to zero (in storage or in cache)
   *
   * @param sessionId id of age zeroing session
   */
  void zeroSessionAge(String sessionId);

  /**
   * Removes session from cache and from storage
   *
   * @param sessionId removing session id
   */
  void removeSession(String sessionId);

  /**
   * Removes all sessions with age is too big. Maximum age defined in {@link SessionServiceBuilder}
   */
  void removeOldSessions();

  /**
   * Synchronizes cache with storage: for each session in cache it verify session state in storage and
   * modify or remove session
   */
  void syncCache();

  /**
   * Retrieves statistics information
   *
   * @return statistics information in map
   */
  Map<String, String> statisticsInfo();
}
