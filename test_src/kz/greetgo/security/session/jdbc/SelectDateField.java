package kz.greetgo.security.session.jdbc;

import kz.greetgo.db.ConnectionCallback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

public class SelectDateField implements ConnectionCallback<Date> {
  private final String fieldName;
  private final String tableName;
  private final String idValue;

  public SelectDateField(String fieldName, String tableName, String idValue) {
    this.fieldName = fieldName;
    this.tableName = tableName;
    this.idValue = idValue;
  }

  @Override
  public Date doInConnection(Connection con) throws Exception {
    try (PreparedStatement ps = con.prepareStatement("select " + fieldName + " from " + tableName + " where id = ?")) {
      ps.setString(1, idValue);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) throw new RuntimeException("No record in " + tableName + " with id = " + idValue);
        Timestamp timestamp = rs.getTimestamp(1);
        if (timestamp == null) return null;
        return new Date(timestamp.getTime());
      }
    }
  }
}
