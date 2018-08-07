package kz.greetgo.security.crypto.jdbc;

public class ContentNames {
  public final String tableName;
  public final String keyField;
  public final String keyValue;
  public final String valueField;

  public ContentNames(String tableName, String keyField, String keyValue, String valueField) {
    this.tableName = tableName;
    this.keyField = keyField;
    this.keyValue = keyValue;
    this.valueField = valueField;
  }
}
