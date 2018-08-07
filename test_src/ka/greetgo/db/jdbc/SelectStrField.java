package ka.greetgo.db.jdbc;

import kz.greetgo.db.ConnectionCallback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SelectStrField implements ConnectionCallback<String> {
  private final String fieldName;
  private final String tableName;
  private final String idValue;

  public SelectStrField(String fieldName, String tableName, String idValue) {
    this.fieldName = fieldName;
    this.tableName = tableName;
    this.idValue = idValue;
  }

  @Override
  public String doInConnection(Connection con) throws Exception {
    try (PreparedStatement ps = con.prepareStatement("select " + fieldName + " from " + tableName + " where id = ?")) {
      ps.setString(1, idValue);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) throw new RuntimeException("No record in " + tableName + " with id = " + idValue);
        return rs.getString(1);
      }
    }
  }
}
