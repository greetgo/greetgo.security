package kz.greetgo.security.session;

import kz.greetgo.security.session.Serializer;
import kz.greetgo.util.RND;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;

public class SerializerTest {

  public static class TestObject implements Serializable {
    public String strField;
    public Date dateField;
    public int intField;
  }

  @Test
  public void serialize_deserialize() {

    TestObject o1 = new TestObject();
    o1.dateField = RND.dateYears(-100, 10);
    o1.intField = RND.plusInt(10_000_000);
    o1.strField = RND.str(10);

    //
    //
    byte[] bytes = Serializer.serialize(o1);
    //
    //

    assertThat(bytes).isNotNull();

    //
    //
    TestObject o2 = Serializer.deserialize(bytes);
    //
    //

    assertThat(o2).isNotNull();
    //noinspection ConstantConditions
    assertThat(o2.dateField).isEqualTo(o1.dateField);
    assertThat(o2.intField).isEqualTo(o1.intField);
    assertThat(o2.strField).isEqualTo(o1.strField);
  }

  @Test
  public void serialize_deserialize_null() {

    //
    //
    byte[] bytes = Serializer.serialize(null);
    //
    //

    assertThat(bytes).isNotNull();

    //
    //
    TestObject o2 = Serializer.deserialize(bytes);
    //
    //

    assertThat(o2).isNull();
  }
}