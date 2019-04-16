package kz.greetgo.security.factory;

import kz.greetgo.db.AbstractJdbcWithDataSource;
import kz.greetgo.db.Jdbc;
import kz.greetgo.db.TransactionManager;
import kz.greetgo.security.crypto.errors.SqlWrapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import static kz.greetgo.conf.sys_params.SysParams.oracleAdminHost;
import static kz.greetgo.conf.sys_params.SysParams.oracleAdminPassword;
import static kz.greetgo.conf.sys_params.SysParams.oracleAdminPort;
import static kz.greetgo.conf.sys_params.SysParams.oracleAdminSid;
import static kz.greetgo.conf.sys_params.SysParams.oracleAdminUserid;
import static org.fest.assertions.api.Assertions.assertThat;

public class OracleFactory {
  public String username;
  private final String password = "111";

  public Jdbc create() {

    try {

      try {
        ping();
      } catch (Exception e) {
        if (e.getMessage().startsWith("ORA-01017:")) {
          createDb();
          ping();
          return directCreateJdbc();
        }

        throw e;
      }

      return directCreateJdbc();

    } catch (SQLException e) {
      throw new SqlWrapper(e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private Jdbc directCreateJdbc() {
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
            try {
              return getUserConnection();
            } catch (ClassNotFoundException e) {
              throw new RuntimeException(e);
            }
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

  private void createDb() throws SQLException, ClassNotFoundException {
    try (Connection con = getOracleAdminConnection()) {

      try {
        exec(con, "alter session set \"_ORACLE_SCRIPT\"=true");
      } catch (SQLException e) {
        //noinspection StatementWithEmptyBody
        if (e.getMessage().startsWith("ORA-02248:")) {
          //ignore
        } else {
          throw e;
        }
      }

      try {
        exec(con, "drop user " + username + " cascade");
      } catch (SQLException e) {
        //noinspection StatementWithEmptyBody
        if (e.getMessage().startsWith("ORA-01918:")) {
          //ignore
        } else {
          throw e;
        }
      }

      exec(con, "create user " + username + " identified by " + password);
      exec(con, "grant all privileges to " + username);

    }
  }

  private void ping() throws ClassNotFoundException, SQLException {


    try (Connection con = getUserConnection()) {

      try (PreparedStatement ps = con.prepareStatement("select 2 from dual")) {
        try (ResultSet rs = ps.executeQuery()) {
          if (!rs.next()) {
            throw new RuntimeException("Left result set");
          }
          assertThat(rs.getInt(1)).isEqualTo(2);
        }
      }
    }


  }

  public static Connection getOracleAdminConnection() throws ClassNotFoundException, SQLException {
    Class.forName("oracle.jdbc.driver.OracleDriver");
    return DriverManager.getConnection(url(), oracleAdminUserid(), oracleAdminPassword());
  }

  private static String url() {
    return "jdbc:oracle:thin:@" + oracleAdminHost() + ":" + oracleAdminPort() + ":" + oracleAdminSid();
  }

  private Connection getUserConnection() throws ClassNotFoundException, SQLException {
    Class.forName("oracle.jdbc.driver.OracleDriver");
    System.out.println("url() = " + url() + ", username = " + username + ", password = " + password);
    return DriverManager.getConnection(url(), username, password);
  }

  private static AtomicReference<Boolean> hasOracleDriverCache = new AtomicReference<>(null);

  public static boolean hasOracleDriver() {
    {
      Boolean cachedValue = hasOracleDriverCache.get();
      if (cachedValue != null) {
        return cachedValue;
      }
    }

    try {

      Class.forName("oracle.jdbc.driver.OracleDriver");
      hasOracleDriverCache.set(true);
      return true;

    } catch (ClassNotFoundException e) {
      hasOracleDriverCache.set(false);
      return false;
    }
  }
}
