package kz.greetgo.security.util;

import java.sql.SQLException;

public class ErrorUtil {
  public static SQLException extractSqlException(Throwable e) {

    while (e != null) {
      if (e instanceof SQLException) {
        return (SQLException) e;
      }

      e = e.getCause();
    }

    return null;
  }
}
