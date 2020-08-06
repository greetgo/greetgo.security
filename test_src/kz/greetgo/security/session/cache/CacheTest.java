package kz.greetgo.security.session.cache;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.fest.assertions.api.Assertions.assertThat;

public class CacheTest {

  @Test
  public void check_caching() {

    AtomicInteger u = new AtomicInteger(147);

    Cache<Integer, String> cache = new CacheBuilder<Integer, String>()
      .nowDateSupplier(Date::new)
      .refreshTimeoutSec(100)
      .maxSize(1000000)
      .loader(i -> "Hello " + (i + u.get()))
      .build();

    for (int i = 0; i < 10; i++) {
      String result = cache.get(i);
      assertThat(result).isEqualTo("Hello " + (i + 147));
    }

    u.set(1171);

    for (int i = 0; i < 10; i++) {
      String result = cache.get(i);
      assertThat(result).isEqualTo("Hello " + (i + 147));
    }

  }

  @Test
  public void check_timeoutRefresh() throws Exception {

    Cache<Integer, Long> cache = new CacheBuilder<Integer, Long>()
      .nowDateSupplier(Date::new)
      .refreshTimeoutSec(1)
      .maxSize(1000000)
      .loader(i -> System.currentTimeMillis())
      .build();

    AtomicBoolean working = new AtomicBoolean(true);
    AtomicBoolean clean = new AtomicBoolean(false);

    class MyThread extends Thread {
      int i = 0;
      final Random rnd = new Random();

      int countMoreSec = 0;
      long maxTime = 0;

      double loopsPerSecond;

      @Override
      public void run() {
        long startedAt = System.nanoTime();
        boolean cleanHere = true;
        while (true) {
          i++;
          if (i % 100 == 0) {
            if (!working.get()) {
              break;
            }
            if (clean.get() && cleanHere) {
              cleanHere = false;
              i = 0;
              countMoreSec = 0;
              maxTime = 0;
            }
          }

          long time = cache.get(rnd.nextInt(3) + 1);
          time = System.currentTimeMillis() - time;

          if (maxTime < time) {
            maxTime = time;
          }

          if (time > 1_000 + 7) {
            countMoreSec++;
          }
        }
        long finishedAt = System.nanoTime();

        loopsPerSecond = (double) i / (finishedAt - startedAt) * 1e9;

      }
    }

    MyThread[] threads = new MyThread[16];
    for (int i = 0; i < threads.length; i++) {
      threads[i] = new MyThread();
    }

    for (MyThread t : threads) {
      t.start();
    }

    Thread.sleep(1000);
    clean.set(true);
    Thread.sleep(2000);
    working.set(false);

    for (MyThread t : threads) {
      t.join();
    }

    for (MyThread t : threads) {
      System.out.println("hLis2c46aN ::" +
        " i = " + t.i + "," +
        " maxTime = " + t.maxTime + "," +
        " countMoreSec = " + t.countMoreSec + "," +
        " loops per second = " + Math.round(t.loopsPerSecond)
      );
    }

    double avgLoopsPerSecond = Arrays.stream(threads).mapToDouble(x -> x.loopsPerSecond).average().orElse(0);
    System.out.println("HzZCcW8Y54 :: avgLoopsPerSecond = " + Math.round(avgLoopsPerSecond));

    for (MyThread t : threads) {
      assertThat(t.countMoreSec).isZero();
    }

  }

  @Test
  public void check_maxSize() throws InterruptedException {

    Cache<Integer, String> cache = new CacheBuilder<Integer, String>()
      .nowDateSupplier(Date::new)
      .refreshTimeoutSec(100000)
      .maxSize(100)
      .loader(i -> "i = " + i)
      .build();

    AtomicBoolean working = new AtomicBoolean(true);
    AtomicBoolean clean = new AtomicBoolean(false);

    final int threadCount = 16;

    CountDownLatch stop1 = new CountDownLatch(threadCount + 1);
    CountDownLatch stop2 = new CountDownLatch(threadCount + 1);

    class MyThread extends Thread {
      int i = 0;
      final Random rnd = new Random();

      @Override
      public void run() {
        while (true) {
          i++;
          if (i % 100 == 0) {
            if (!working.get()) break;
            if (clean.get()) {
              stop1.countDown();
              i = 0;
              stop2.countDown();
            }
          }
          cache.get(rnd.nextInt(500));
        }
      }
    }

    List<MyThread> threads = Stream
      .iterate(1, i -> i + 1).limit(threadCount)
      .map(i -> new MyThread()).collect(Collectors.toList());

    for (MyThread t : threads) {
      t.start();
    }

    Thread.sleep(1000);
    stop1.countDown();
    cache.invalidateAll();
    stop2.countDown();

    class Row {
      final double time;
      final int size;

      public Row(double time, int size) {
        this.time = time;
        this.size = size;
      }
    }
    List<Row> rows = new ArrayList<>();
    long startedAt = System.nanoTime();
    for (int i = 0; i < 200; i++) {
      Thread.sleep(10);
      double time = (System.nanoTime() - startedAt) / 1e9;
      int size = cache.size();
      rows.add(new Row(time, size));
    }

    working.set(false);

    for (MyThread t : threads) {
      t.join();
    }

    for (Row row : rows) {
      System.out.println("1QV3X05bN7 :: time = " + row.time + " sec, size = " + row.size);
    }

    for (Row row : rows) {
      assertThat(row.size).isLessThan(120);
    }

  }

  @Test
  public void check_dying_is_oldest() throws InterruptedException {

    AtomicInteger u = new AtomicInteger(10_000);

    Cache<Integer, String> cache = new CacheBuilder<Integer, String>()
      .nowDateSupplier(Date::new)
      .refreshTimeoutSec(1000000000000000000L)
      .maxSize(10)
      .loader(i -> "result " + (u.get() + i))
      .build();

    for (int i = 1; i <= 20; i++) {
      cache.get(i);
      Thread.sleep(10);
    }

    u.set(70_000);

    for (int i = 1; i <= 3; i++) {
      String str = cache.get(i);
      assertThat(str).isEqualTo("result " + (70_000 + i));
    }

    for (int i = 17; i <= 20; i++) {
      String str = cache.get(i);
      assertThat(str).isEqualTo("result " + (10_000 + i));
    }
  }
}
