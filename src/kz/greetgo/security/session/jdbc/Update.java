package kz.greetgo.security.session.jdbc;

import kz.greetgo.db.ConnectionCallback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class Update implements ConnectionCallback<Integer> {
  private final String sql;
  private final List<Object> sqlParams;

  public Update(String sql, List<Object> sqlParams) {
    this.sql = sql;
    this.sqlParams = sqlParams;
  }

  public Update(String sql) {
    this(sql, new ArrayList<>());
  }

  @Override
  public Integer doInConnection(Connection con) throws Exception {
    try (PreparedStatement ps = con.prepareStatement(sql)) {

      int index = 1;
      for (Object param : sqlParams) {
        ps.setObject(index++, param);
      }

      return ps.executeUpdate();
    }
  }
}
