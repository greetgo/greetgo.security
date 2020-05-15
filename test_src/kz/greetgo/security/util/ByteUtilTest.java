package kz.greetgo.security.util;

import kz.greetgo.util.RND;
import org.testng.annotations.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ByteUtilTest {

  @Test
  public void xorBytes__equalSize() {
    byte[] bytes1 = RND.byteArray(10);
    byte[] bytes2 = RND.byteArray(bytes1.length);

    //
    //
    byte[] actualBytes = ByteUtil.xorBytes(bytes1, bytes2);
    //
    //

    assertThat(actualBytes).hasSize(bytes1.length);
    for (int i = 0; i < bytes1.length; i++) {
      byte byte1 = bytes1[i];
      byte byte2 = bytes2[i];
      byte expected = (byte) (byte1 ^ byte2);
      byte actual = actualBytes[i];
      String d = "i = " + i;
      assertThat(actual).describedAs(d).isEqualTo(expected);
    }
  }

  @Test
  public void copyToLength__less() {

    byte[] source = RND.byteArray(100);

    //
    //
    byte[] result = ByteUtil.copyToLength(source, 50);
    //
    //

    assertThat(result).hasSize(50);

    for (int i = 0; i < 50; i++) {
      String d = "i = " + i;
      assertThat(result[i]).describedAs(d).isEqualTo(source[i]);
    }
  }

  @Test
  public void copyToLength__eq() {

    byte[] source = RND.byteArray(100);

    //
    //
    byte[] result = ByteUtil.copyToLength(source, 100);
    //
    //

    assertThat(result).hasSize(100);

    for (int i = 0; i < 100; i++) {
      String d = "i = " + i;
      assertThat(result[i]).describedAs(d).isEqualTo(source[i]);
    }
  }

  @Test
  public void copyToLength__more() {

    byte[] source = RND.byteArray(100);

    //
    //
    byte[] result = ByteUtil.copyToLength(source, 150);
    //
    //

    assertThat(result).hasSize(150);

    for (int i = 0; i < 100; i++) {
      String d = "i = " + i;
      assertThat(result[i]).describedAs(d).isEqualTo(source[i]);
    }
    for (int i = 100; i < 150; i++) {
      String d = "i = " + i;
      assertThat(result[i]).describedAs(d).isEqualTo(source[i - 100]);
    }
  }

  @Test
  public void copyToLength__more3() {

    byte[] source = RND.byteArray(100);

    //
    //
    byte[] result = ByteUtil.copyToLength(source, 350);
    //
    //

    assertThat(result).hasSize(350);

    for (int i = 0; i < 100; i++) {
      String d = "i = " + i;
      assertThat(result[i]).describedAs(d).isEqualTo(source[i]);
    }
    for (int i = 100; i < 200; i++) {
      String d = "i = " + i;
      assertThat(result[i]).describedAs(d).isEqualTo(source[i - 100]);
    }
    for (int i = 200; i < 300; i++) {
      String d = "i = " + i;
      assertThat(result[i]).describedAs(d).isEqualTo(source[i - 200]);
    }
    for (int i = 300; i < 350; i++) {
      String d = "i = " + i;
      assertThat(result[i]).describedAs(d).isEqualTo(source[i - 300]);
    }
  }
}
