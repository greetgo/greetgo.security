package kz.greetgo.security.crypto.jdbc;

import kz.greetgo.db.Jdbc;
import kz.greetgo.security.crypto.ContentAccess;
import kz.greetgo.security.crypto.errors.SqlWrapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcContentAccess implements ContentAccess {

  private final Jdbc jdbc;
  private final ContentNames names;
  private final String createTableDdl;
  private final DbDialect dialect;

  public JdbcContentAccess(Jdbc jdbc, ContentNames names, String createTableDdl, DbDialect dialect) {
    this.jdbc = jdbc;
    this.names = names;
    this.createTableDdl = createTableDdl;
    this.dialect = dialect;
  }

  @Override
  public byte[] downloadBytes() {
    return jdbc.execute(con -> {
      String sql = "select " + names.valueFieldName + " from " + names.tableName + " where " + names.idFieldName + " = ?";
      try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setString(1, names.idValue);
        try (ResultSet rs = ps.executeQuery()) {
          if (!rs.next()) {
            return null;
          }
          return rs.getBytes(1);
        }
      }
    });
  }

  @Override
  public void uploadBytes(byte[] bytes) {
    jdbc.execute(con -> {

      try {
        if (bytes == null) {
          delete(con);
          return null;
        }

        if (update(con, bytes) > 0) {
          return null;
        }
      } catch (SQLException e) {
        if (dialect.isNoTable(e)) {

          createTable(con);

        } else throw new SqlWrapper(e);
      }

      try {
        insert(con, bytes);
      } catch (SQLException e) {

        if (dialect.isRecordAlreadyExists(e)) {

          update(con, bytes);

        } else throw new SqlWrapper(e);
      }

      return null;
    });
  }

  private void delete(Connection con) throws SQLException {
    String sql = "delete from " + names.tableName + " where " + names.idFieldName + " = ?";
    try (PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, names.idValue);
      ps.executeUpdate();
    }
  }

  private void createTable(Connection con) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(createTableDdl)) {
      ps.executeUpdate();
    }
  }

  private int update(Connection con, byte[] bytes) throws SQLException {
    String sql = "update " + names.tableName + " set " + names.valueFieldName + " = ? where " + names.idFieldName + " = ?";
    try (PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setBytes(1, bytes);
      ps.setString(2, names.idValue);
      return ps.executeUpdate();
    }
  }

  private void insert(Connection con, byte[] bytes) throws SQLException {
    String sql = "insert into " + names.tableName + " (" + names.idFieldName + ", " + names.valueFieldName + ") values (?, ?)";
    try (PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, names.idValue);
      ps.setBytes(2, bytes);
      ps.executeUpdate();
    }
  }

  @Override
  public boolean exists() {
    return jdbc.execute(con -> {
      try {
        return checkExists(con);
      } catch (SQLException e) {
        if (dialect.isNoTable(e)) {
          createTable(con);
          return checkExists(con);
        }
        throw e;
      }
    });
  }

  private Boolean checkExists(Connection con) throws SQLException {
    String sql = "select count(1) from " + names.tableName + " where " + names.idFieldName + " = ?";
    try (PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, names.idValue);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) throw new RuntimeException("FATAL ERROR");
        return rs.getInt(1) > 0;
      }
    }
  }
}
