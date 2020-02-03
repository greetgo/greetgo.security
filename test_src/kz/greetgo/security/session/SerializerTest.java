package kz.greetgo.security.session;

import kz.greetgo.security.errors.SerializedClassChanged;
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

  @Test
  public void serializeToStr_deserializeFromStr() {
    TestObject o1 = new TestObject();
    o1.dateField = RND.dateYears(-100, 10);
    o1.intField = RND.plusInt(10_000_000);
    o1.strField = RND.str(10);

    //
    //
    String serializedStr = Serializer.serializeToStr(o1);
    //
    //

    assertThat(serializedStr).isNotNull();
    assertThat(serializedStr).isNotEmpty();

    //
    //
    TestObject o2 = Serializer.deserializeFromStr(serializedStr);
    //
    //

    assertThat(o2).isNotNull();
    assertThat(o2.dateField).isEqualTo(o1.dateField);
    assertThat(o2.intField).isEqualTo(o1.intField);
    assertThat(o2.strField).isEqualTo(o1.strField);
  }

  @Test
  public void serializeToStr_deserializeFromStr_null() {

    //
    //
    String serializedStr = Serializer.serializeToStr(null);
    //
    //

    assertThat(serializedStr).isNotNull();

    //
    //
    TestObject o2 = Serializer.deserializeFromStr(serializedStr);
    //
    //

    assertThat(o2).isNull();
  }

  @Test(expectedExceptions = SerializedClassChanged.class)
  public void serializeToStr_leftClassName() {

    //noinspection SpellCheckingInspection
    String serializedStr1 = "rO0ABXNyADZrei5ncmVldGdvLnNlY3VyaXR5LnNlc3Npb24uU2VyaWFsaXplclRlc3QkVGVzdE9iamVjdDEm" +
      "TymSeA$X$QIAA0kACGludEZpZWxkTAAJZGF0ZUZpZWxkdAAQTGphdmEvdXRpbC9EYXRlO0wACHN0ckZpZWxkdAASTGphdmEvbGFuZy9TdH" +
      "Jpbmc7eHAAD5onc3IADmphdmEudXRpbC5EYXRlaGqBAUtZdBkDAAB4cHcI$$$$lsPLpjd4dAAP0YTQqdCfV9CwQtCec1Jt";

    //
    //
    Serializer.deserializeFromStr(serializedStr1);
    //
    //

  }

}
