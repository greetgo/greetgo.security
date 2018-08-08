package kz.greetgo.security.crypto.jdbc;

import kz.greetgo.security.crypto.jdbc.create_table.FieldType;

import java.sql.SQLException;

public class DbDialectPostgres extends AbstractDbDialect {

  @Override
  public boolean isNoTable(SQLException e) {
    return "42P01".equals(e.getSQLState());
  }

  @Override
  public boolean isRecordAlreadyExists(SQLException e) {
    return "23505".equals(e.getSQLState());
  }

  @Override
  protected String typeToStr(FieldType type, int length) {
    switch (type) {
      case Str:
        return "varchar(" + length + ")";
      case Blob:
        return "byTea";
      default:
        throw new RuntimeException("Unsupported type = " + type);
    }
  }
}
