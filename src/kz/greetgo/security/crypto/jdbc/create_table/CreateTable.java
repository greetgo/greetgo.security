package kz.greetgo.security.crypto.jdbc.create_table;

import java.util.ArrayList;
import java.util.List;

public class CreateTable {
  public final String tableName;

  public CreateTable(String tableName) {
    this.tableName = tableName;
  }

  public final List<CreatingField> creatingFields = new ArrayList<>();

  public CreatingField newField(String fieldName) {
    CreatingField creatingField = new CreatingField(fieldName);
    creatingFields.add(creatingField);
    return creatingField;
  }
}
