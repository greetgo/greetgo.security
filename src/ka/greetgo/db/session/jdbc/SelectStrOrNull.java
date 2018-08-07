package ka.greetgo.db.session.jdbc;

import kz.greetgo.db.ConnectionCallback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SelectStrOrNull implements ConnectionCallback<String> {

  private final String sql;
  private final List<Object> sqlParams;

  public SelectStrOrNull(String sql, List<Object> sqlParams) {
    this.sql = sql;
    this.sqlParams = sqlParams;
  }

  public SelectStrOrNull(String sql) {
    this(sql, new ArrayList<>());
  }

  @Override
  public String doInConnection(Connection con) throws Exception {
    try (PreparedStatement ps = con.prepareStatement(sql)) {
      int index = 1;
      for (Object param : sqlParams) {
        ps.setObject(index++, param);
      }
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return null;
        return rs.getString(1);
      }
    }
  }
}
