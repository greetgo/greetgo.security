package kz.greetgo.security.crypto.jdbc.create_table;

public class CreatingField {
  public final String name;
  public FieldType type;
  public int length;
  public boolean primaryKey;
  public boolean notNull = false;

  public CreatingField(String name) {
    this.name = name;
  }

  public CreatingField typeString(int length) {
    type = FieldType.Str;
    this.length = length;
    return this;
  }

  public CreatingField primaryKey() {
    primaryKey = true;
    return this;
  }

  public CreatingField typeBlob() {
    type = FieldType.Blob;
    return this;
  }

  public CreatingField notNull() {
    notNull = true;
    return this;
  }

  public CreatingField setNotNull(boolean notNull) {
    this.notNull = notNull;
    return this;
  }
}
