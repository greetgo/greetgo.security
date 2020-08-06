package kz.greetgo.security.session;

import kz.greetgo.security.crypto.Crypto;

import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

public class SessionServiceBuilder {
  SessionStorage storage;
  SaltGenerator saltGenerator;

  int oldSessionAgeInHours = 24;
  int sessionIdLength = 15;
  int tokenLength = 15;

  LongSupplier lastTouchedCacheTimeoutSec = () -> 30;
  IntSupplier lastTouchedCacheSize = () -> 1_000_000;

  private SessionServiceBuilder() {}

  public static SessionServiceBuilder newBuilder() {
    return new SessionServiceBuilder();
  }

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

  public SessionServiceBuilder setSaltGeneratorOnCrypto(Crypto crypto, int saltLength, byte[] saltMixture) {
    checkBuilt();
    this.saltGenerator = new SaltGeneratorCryptoBridge(crypto, saltLength, saltMixture);
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

  public SessionServiceBuilder setLastTouchedCacheTimeoutSec(LongSupplier lastTouchedCacheTimeoutSec) {
    this.lastTouchedCacheTimeoutSec = Objects.requireNonNull(lastTouchedCacheTimeoutSec);
    return this;
  }

  public SessionServiceBuilder setLastTouchedCacheTimeoutSec(long dbCacheTimeoutSec) {
    return setLastTouchedCacheTimeoutSec(() -> dbCacheTimeoutSec);
  }

  public SessionServiceBuilder setLastTouchedCacheSize(IntSupplier lastTouchedCacheSize) {
    this.lastTouchedCacheSize = Objects.requireNonNull(lastTouchedCacheSize);
    return this;
  }

  public SessionServiceBuilder setLastTouchedCacheSize(int lastTouchedCacheSize) {
    return setLastTouchedCacheSize(() -> lastTouchedCacheSize);
  }

  public SessionService build() {
    built = true;
    if (storage == null) throw new RuntimeException("No sessionStorage");
    if (saltGenerator == null) throw new RuntimeException("No saltGenerator");
    return new SessionServiceImpl(this);
  }
}
