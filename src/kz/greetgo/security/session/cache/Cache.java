package kz.greetgo.security.session.cache;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

public class Cache<Input extends Comparable<Input>, Output> {

  private final LongSupplier refreshTimeoutSec;
  private final IntSupplier maxSize;
  private final Function<Input, Output> loader;
  private final LongSupplier nowMillisSupplier;

  public Cache(LongSupplier refreshTimeoutSec, IntSupplier maxSize, Function<Input, Output> loader,
               LongSupplier nowMillisSupplier) {
    this.refreshTimeoutSec = refreshTimeoutSec;
    this.maxSize = maxSize;
    this.loader = loader;
    this.nowMillisSupplier = nowMillisSupplier;
  }

  private long nowMillis() {
    return nowMillisSupplier.getAsLong();
  }

  private long refreshTimeoutSec() {
    return refreshTimeoutSec.getAsLong();
  }

  private int maxSize() {
    return maxSize.getAsInt();
  }

  private Output load(Input input) {
    return loader.apply(input);
  }


  private static class TimeInput<Input extends Comparable<Input>> implements Comparable<TimeInput<Input>> {
    final long timeMillis;
    final Input input;

    private TimeInput(long timeMillis, Input input) {
      this.timeMillis = timeMillis;
      this.input = input;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      TimeInput<?> timeInput = (TimeInput<?>) o;
      return timeMillis == timeInput.timeMillis && Objects.equals(input, timeInput.input);
    }

    @Override
    public int hashCode() {
      return Objects.hash(timeMillis, input);
    }

    @Override
    public int compareTo(TimeInput<Input> o) {
      if (timeMillis != o.timeMillis) {
        return timeMillis - o.timeMillis < 0 ? -1 : 1;
      }

      if (input == null) {
        return o.input == null ? 0 : -1;
      }
      if (o.input == null) {
        return 1;
      }

      return input.compareTo(o.input);
    }

    @Override
    public String toString() {
      return "TimeInput{" + timeMillis + " : " + input + '}';
    }
  }

  private static class Dot<Output> {
    final Output output;
    final long timeMillis;

    public Dot(Output output, long timeMillis) {
      this.output = output;
      this.timeMillis = timeMillis;
    }

    @Override
    public String toString() {
      return "Dot{" + timeMillis + ": " + output + '}';
    }
  }

  private final ConcurrentNavigableMap<TimeInput<Input>, TimeInput<Input>> set = new ConcurrentSkipListMap<>();
  private final ConcurrentMap<Input, Dot<Output>> map = new ConcurrentHashMap<>();

  public void invalidateAll() {
    set.clear();
    map.clear();
  }

  public int size() {
    return map.size();
  }

  public Output get(Input input) {
    long nowMillis = nowMillis();

    Dot<Output> outputDot = map.get(input);
    if (outputDot != null) {

      long refreshTimeoutMillis = refreshTimeoutSec() * 1000;

      long timeMillis = outputDot.timeMillis;

      if (nowMillis - timeMillis <= refreshTimeoutMillis) {
        correctSize();
        return outputDot.output;
      }

      set.remove(new TimeInput<>(timeMillis, input));
    }

    {
      Output output = load(input);

      Dot<Output> newOutputDot = new Dot<>(output, nowMillis);
      TimeInput<Input> timeInput = new TimeInput<>(nowMillis, input);

      map.put(input, newOutputDot);
      set.put(timeInput, timeInput);

      correctSize();
      return output;
    }
  }

  private void correctSize() {
    int maxSize = maxSize();
    while (set.size() > maxSize) {
      TimeInput<Input> first = set.keySet().pollFirst();
      if (first == null) return;
      map.remove(first.input);
    }
  }

}
