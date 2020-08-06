package kz.greetgo.security.session.cache;

import java.util.Date;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class CacheBuilder<Input extends Comparable<Input>, Output> {
  private LongSupplier refreshTimeoutSec;
  private IntSupplier maxSize;
  private Function<Input, Output> loader;
  private LongSupplier nowMillisSupplier = System::currentTimeMillis;

  public CacheBuilder<Input, Output> nowMillisSupplier(LongSupplier nowSupplier) {
    this.nowMillisSupplier = requireNonNull(nowSupplier);
    return this;
  }

  public CacheBuilder<Input, Output> nowDateSupplier(Supplier<Date> nowSupplier) {
    requireNonNull(nowSupplier);
    return nowMillisSupplier(() -> requireNonNull(nowSupplier.get()).getTime());
  }

  public CacheBuilder<Input, Output> refreshTimeoutSec(LongSupplier refreshTimeoutSec) {
    this.refreshTimeoutSec = refreshTimeoutSec;
    return this;
  }

  public CacheBuilder<Input, Output> refreshTimeoutSec(long refreshTimeoutSec) {
    return refreshTimeoutSec(() -> refreshTimeoutSec);
  }

  public CacheBuilder<Input, Output> maxSize(IntSupplier maxSize) {
    this.maxSize = maxSize;
    return this;
  }

  public CacheBuilder<Input, Output> maxSize(int maxSize) {
    return maxSize(() -> maxSize);
  }

  public CacheBuilder<Input, Output> loader(Function<Input, Output> loader) {
    this.loader = loader;
    return this;
  }

  public Cache<Input, Output> build() {
    requireNonNull(refreshTimeoutSec);
    requireNonNull(maxSize);
    requireNonNull(loader);
    return new Cache<>(refreshTimeoutSec, maxSize, loader, nowMillisSupplier);
  }

}
