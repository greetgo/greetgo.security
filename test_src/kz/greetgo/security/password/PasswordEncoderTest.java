package kz.greetgo.security.password;

import kz.greetgo.util.RND;
import org.testng.annotations.Test;

import static kz.greetgo.security.SecurityBuilders.newPasswordEncoderBuilder;
import static org.fest.assertions.api.Assertions.assertThat;

public class PasswordEncoderTest {
  @Test
  public void encode_verify() {

    PasswordEncoder passwordEncoder = newPasswordEncoderBuilder()
      .setSalt("asd234yry5654o32l56")
      .build();

    //
    //
    String encodedPassword1 = passwordEncoder.encode("111");
    String encodedPassword2 = passwordEncoder.encode("111");
    //
    //

    assertThat(encodedPassword1).isEqualTo(encodedPassword2);

    {
      boolean verifyResult = passwordEncoder.verify("111", encodedPassword1);
      assertThat(verifyResult).isTrue();
    }
    {
      boolean verifyResult = passwordEncoder.verify("222", encodedPassword1);
      assertThat(verifyResult).isFalse();
    }
  }

  @Test
  public void encode_verify_nullAndEmpty() {

    PasswordEncoder passwordEncoder = newPasswordEncoderBuilder()
      .setSalt(RND.str(10))
      .build();

    //
    //
    String encodedPassword1 = passwordEncoder.encode(null);
    String encodedPassword2 = passwordEncoder.encode("");
    //
    //

    assertThat(encodedPassword1).isEqualTo(encodedPassword2);

    {
      boolean verifyResult = passwordEncoder.verify(null, encodedPassword1);
      assertThat(verifyResult).isTrue();
    }
    {
      boolean verifyResult = passwordEncoder.verify("222", encodedPassword1);
      assertThat(verifyResult).isFalse();
    }
    {
      boolean verifyResult = passwordEncoder.verify("", encodedPassword1);
      assertThat(verifyResult).isTrue();
    }
  }
}