package kz.greetgo.mvc.security;

/**
 * Determines security logic
 */
public interface SecurityProvider {
  /**
   * @return cookie parameter name for storing session data
   */
  String cookieKeySession();

  /**
   * @return cookie parameter name for storing session signing data
   */
  String cookieKeySignature();

  /**
   * Indicates targets under security umbrella
   *
   * @param target current target
   * @return necessary of this target security : <code>true</code> - yes, this target under security
   * (checked and prepared data of session); <code>false</code> - session check is absent, a decision
   * about session preparing will be permitted after call of method {@link #skipSession(String)}
   */
  boolean isUnderSecurityUmbrella(String target);

  /**
   * Determine target. On this target the system will be redirected when some security error happened
   *
   * @param target current target
   * @return redirect target
   */
  String redirectOnSecurityError(String target);

  /**
   * Указывает таргеты, для которых сессию подготавливать не нужно (например для картинок, или статических css-файлов)
   * Determine targets, where there is no needs of session data
   *
   * @param target target
   * @return If returns <code>true</code>, then session will not be prepared, else - session will be prepared
   */
  boolean skipSession(String target);
}
