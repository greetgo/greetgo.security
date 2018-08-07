package ka.greetgo.db.session;

public class SessionServiceBuilder {
  SessionStorage storage;
  SaltGenerator saltGenerator;

  int oldSessionAgeInHours = 24;
  int sessionIdLength = 15;
  int tokenLength = 15;

  SessionServiceBuilder() {}

  public SessionServiceBuilder setStorage(SessionStorage storage) {
    checkBuilt();
    this.storage = storage;
    return this;
  }

  private boolean built = false;

  private void checkBuilt() {
    if (built) throw new RuntimeException("Already built");
  }

  public SessionServiceBuilder setSaltGenerator(SaltGenerator saltGenerator) {
    checkBuilt();
    this.saltGenerator = saltGenerator;
    return this;
  }

  public SessionServiceBuilder setOldSessionAgeInHours(int oldSessionAgeInHours) {
    checkBuilt();
    this.oldSessionAgeInHours = oldSessionAgeInHours;
    return this;
  }

  public SessionServiceBuilder setSessionIdLength(int sessionIdLength) {
    checkBuilt();
    this.sessionIdLength = sessionIdLength;
    return this;
  }

  public SessionServiceBuilder setTokenLength(int tokenLength) {
    checkBuilt();
    this.tokenLength = tokenLength;
    return this;
  }

  public SessionService build() {
    built = true;
    if (storage == null) throw new RuntimeException("No sessionStorage");
    if (saltGenerator == null) throw new RuntimeException("No saltGenerator");
    return new SessionServiceImpl(this);
  }
}
