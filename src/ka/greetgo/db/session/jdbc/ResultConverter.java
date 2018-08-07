package ka.greetgo.db.session.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultConverter<T> {
  T convert(ResultSet rs) throws SQLException;
}
