package kz.greetgo.security.session.jdbc;

import kz.greetgo.db.ConnectionCallback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class SelectFirstOrNull<T> implements ConnectionCallback<T> {

  private final String sql;
  private final List<Object> sqlParams;
  private final ResultConverter<T> resultConverter;

  public SelectFirstOrNull(String sql, List<Object> sqlParams, ResultConverter<T> resultConverter) {
    this.sql = sql;
    this.sqlParams = sqlParams;
    this.resultConverter = resultConverter;
  }

  @Override
  public T doInConnection(Connection con) throws Exception {

    try (PreparedStatement ps = con.prepareStatement(sql)) {
      int index = 1;
      for (Object param : sqlParams) {
        ps.setObject(index++, param);
      }
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return null;
        return resultConverter.convert(rs);
      }
    }

  }
}
