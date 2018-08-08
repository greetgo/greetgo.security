package kz.greetgo.security.jdbc;

import kz.greetgo.db.ConnectionCallback;
import kz.greetgo.db.DbType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class SelectNow implements ConnectionCallback<Date> {
  @Override
  public Date doInConnection(Connection con) throws Exception {


    try (PreparedStatement ps = con.prepareStatement(nowSql(con))) {
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) throw new RuntimeException("FATAL SQL ERROR");
        return new Date(rs.getTimestamp(1).getTime());
      }
    }
  }

  private static String nowSql(Connection con) throws SQLException {
    switch (DbType.detect(con)) {
      case Postgres:
        return "select clock_timestamp()";

      case Oracle:
        return "select current_timestamp from dual";

      default:
        throw new RuntimeException("Unsupported db " + con.getMetaData().getDatabaseProductName());
    }
  }
}
