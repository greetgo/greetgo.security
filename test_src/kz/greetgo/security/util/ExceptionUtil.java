package kz.greetgo.security.util;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class ExceptionUtil {
  public static SQLException extractSqlException(Throwable e) {
    Set<Throwable> remembered = new HashSet<>();

    Throwable current = e;

    while (true) {

      if (current == null) {
        return null;
      }

      if (remembered.contains(current)) {
        return null;
      }
      remembered.add(current);

      if (current instanceof SQLException) {
        return (SQLException) current;
      }

      current = current.getCause();

    }
  }
}
