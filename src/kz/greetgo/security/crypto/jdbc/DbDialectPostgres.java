package kz.greetgo.security.crypto.jdbc;

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
}
