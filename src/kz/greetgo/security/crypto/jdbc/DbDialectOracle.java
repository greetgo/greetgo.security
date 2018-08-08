package kz.greetgo.security.crypto.jdbc;

import kz.greetgo.security.crypto.jdbc.create_table.FieldType;

import java.sql.SQLException;

public class DbDialectOracle extends AbstractDbDialect {

  @Override
  public boolean isNoTable(SQLException e) {
    return e.getMessage().startsWith("ORA-00942:");
  }

  @Override
  public boolean isRecordAlreadyExists(SQLException e) {
    return e.getMessage().startsWith("ORA-00001:");
  }

  @Override
  protected String typeToStr(FieldType type, int length) {
    switch (type) {
      case Str:
        return "varchar2(" + length + ")";
      case Blob:
        return "blob";
      default:
        throw new RuntimeException("Unsupported type = " + type);
    }
  }
}
