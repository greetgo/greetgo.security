package kz.greetgo.security.crypto.jdbc;

public class ContentNames {
  public final String tableName;
  public final String idFieldName;
  public final String idValue;
  public final String valueFieldName;

  public ContentNames(String tableName, String idFieldName, String idValue, String valueFieldName) {
    this.tableName = tableName;
    this.idFieldName = idFieldName;
    this.idValue = idValue;
    this.valueFieldName = valueFieldName;
  }
}
