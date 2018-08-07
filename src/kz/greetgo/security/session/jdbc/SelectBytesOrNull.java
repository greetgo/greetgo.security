package kz.greetgo.security.session.jdbc;

import kz.greetgo.db.ConnectionCallback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class SelectBytesOrNull implements ConnectionCallback<byte[]> {

  private final String sql;
  private final List<Object> sqlParams;

  public SelectBytesOrNull(String sql, List<Object> sqlParams) {
    this.sql = sql;
    this.sqlParams = sqlParams;
  }

  @Override
  public byte[] doInConnection(Connection con) throws Exception {
    try (PreparedStatement ps = con.prepareStatement(sql)) {
      int index = 1;
      for (Object param : sqlParams) {
        ps.setObject(index++, param);
      }
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return null;
        return rs.getBytes(1);
      }
    }
  }
}
