package kz.greetgo.security.crypto.errors;

import java.sql.SQLException;

public class SqlWrapper extends RuntimeException {
  public String sqlState;

  public SqlWrapper(SQLException e) {
    super("SQL State = " + e.getSQLState(), e);
    sqlState = e.getSQLState();
  }
}
