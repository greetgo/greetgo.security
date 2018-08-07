package kz.greetgo.security.factory;

import kz.greetgo.db.AbstractJdbcWithDataSource;
import kz.greetgo.db.Jdbc;
import kz.greetgo.db.TransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static kz.greetgo.conf.sys_params.SysParams.pgAdminPassword;
import static kz.greetgo.conf.sys_params.SysParams.pgAdminUrl;
import static kz.greetgo.conf.sys_params.SysParams.pgAdminUserid;
import static org.fest.assertions.api.Assertions.assertThat;

class PostgresFactory {
  public String dbName;
  private final String password = "111";

  private static String changeUrlDbName(String url, String dbName) {
    int idx = url.lastIndexOf('/');
    return url.substring(0, idx + 1) + dbName;
  }

  public Jdbc create() {

    try {

      try {
        ping();
      } catch (SQLException e) {
        if ("28P01".equals(e.getSQLState())) {
          createDb();
          ping();
          return directCreateJdbc();
        }

        throw e;
      }

      return directCreateJdbc();

    } catch (SQLException e) {
      throw new RuntimeException("SQL State = " + e.getSQLState(), e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private Jdbc directCreateJdbc() throws SQLException {
    return new AbstractJdbcWithDataSource() {
      @Override
      protected TransactionManager getTransactionManager() {
        return null;
      }

      @Override
      protected DataSource getDataSource() {
        return new AbstractDataSource() {
          @Override
          public Connection getConnection() throws SQLException {
            return DriverManager.getConnection(
              changeUrlDbName(pgAdminUrl(), dbName), dbName, password);
          }
        };
      }
    };
  }

  private static void exec(Connection con, String sql) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(sql)) {
      ps.executeUpdate();
    }
  }

  private void createDb() throws SQLException {
    try (Connection con = DriverManager.getConnection(pgAdminUrl(), pgAdminUserid(), pgAdminPassword())) {

      exec(con, "create user " + dbName + " with password '" + password + "'");
      exec(con, "create database " + dbName + " with owner " + dbName);

    }
  }

  private void ping() throws ClassNotFoundException, SQLException {
    Class.forName("org.postgresql.Driver");
    try (Connection con = DriverManager.getConnection(
      changeUrlDbName(pgAdminUrl(), dbName), dbName, password)) {

      try (PreparedStatement ps = con.prepareStatement("select 2")) {
        try (ResultSet rs = ps.executeQuery()) {
          if (!rs.next()) throw new RuntimeException("Left result set");
          assertThat(rs.getInt(1)).isEqualTo(2);
        }
      }
    }
  }
}
