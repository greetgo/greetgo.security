package kz.greetgo.security.session.jdbc;

import kz.greetgo.db.ConnectionCallback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

public class SelectNow implements ConnectionCallback<Date> {
  @Override
  public Date doInConnection(Connection con) throws Exception {
    try (PreparedStatement ps = con.prepareStatement("select current_timestamp")) {
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) throw new RuntimeException("FATAL SQL ERROR");
        return new Date(rs.getTimestamp(1).getTime());
      }
    }
  }
}
