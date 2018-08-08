package kz.greetgo.security.crypto.jdbc;

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
}
