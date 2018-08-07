package ka.greetgo.db.session.jdbc;

import kz.greetgo.db.ConnectionCallback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SelectDateOrNull implements ConnectionCallback<Date> {

  private final String sql;
  private final List<Object> sqlParams;

  public SelectDateOrNull(String sql, List<Object> sqlParams) {
    this.sql = sql;
    this.sqlParams = sqlParams;
  }

  public SelectDateOrNull(String sql) {
    this(sql, new ArrayList<>());
  }

  @Override
  public Date doInConnection(Connection con) throws Exception {
    try (PreparedStatement ps = con.prepareStatement(sql)) {
      int index = 1;
      for (Object param : sqlParams) {
        ps.setObject(index++, param);
      }
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return null;
        Timestamp timestamp = rs.getTimestamp(1);
        if (timestamp == null) return null;
        return new Date(timestamp.getTime());
      }
    }
  }
}
