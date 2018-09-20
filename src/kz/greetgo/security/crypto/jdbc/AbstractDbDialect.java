package kz.greetgo.security.crypto.jdbc;

import kz.greetgo.security.crypto.jdbc.create_table.CreateTable;
import kz.greetgo.security.crypto.jdbc.create_table.CreatingField;
import kz.greetgo.security.crypto.jdbc.create_table.FieldType;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

public abstract class AbstractDbDialect implements DbDialect {
  @Override
  public String generateCreateTableDDL(CreateTable createTable) {

    List<String> fieldStrList = new ArrayList<>();

    {
      List<String> primaryKeyNames = new ArrayList<>();

      for (CreatingField f : createTable.creatingFields) {
        if (f.primaryKey) primaryKeyNames.add(f.name);
        fieldStrList.add(f.name + " " + typeToStr(f.type, f.length) + (f.notNull ? " not null" : ""));
      }

      if (primaryKeyNames.size() > 0) {
        fieldStrList.add("primary key (" + primaryKeyNames.stream().collect(joining(", ")) + ")");
      }
    }


    return "create table " + createTable.tableName + " (" + fieldStrList.stream().collect(joining(", ")) + ")";
  }

  protected abstract String typeToStr(FieldType type, int length);
}
