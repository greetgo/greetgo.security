package kz.greetgo.security.crypto.jdbc;

import java.sql.SQLException;

public interface DbDialect {

  boolean isNoTable(SQLException e);

  boolean isRecordAlreadyExists(SQLException e);
}
