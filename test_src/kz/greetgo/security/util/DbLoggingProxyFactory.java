///MODIFY replace sandbox {PROJECT_NAME}
package kz.greetgo.security.util;

import kz.greetgo.db.ConnectionCallback;
import kz.greetgo.db.Jdbc;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DbLoggingProxyFactory {

  public interface SqlViewer {
    void gotConnection(String connectionId, long callNanos);

    void connectionSetAutoCommit(String connectionId, boolean autoCommit);

    void connectionClose(String connectionId);

    void connectionPrepareStatement(String connectionId, long callNanos, String sql, Object... anotherArgs);

    void statementClose(String connectionId);

    void statementSetParameter(String connectionId, String methodName, int index, Object arg);

    void statementExecute(String connectionId, String methodName, long callNanos);
  }

  private static final double GIG = 1_000_000_000;

  @SuppressWarnings("StringConcatenationInLoop")
  private static String callNanos(long callNanos) {
    double value = (double) callNanos / GIG;
    DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.CANADA);
    dfs.setGroupingSeparator(' ');
    DecimalFormat df = new DecimalFormat("###,##0.000000", dfs);
    return " " + df.format(value) + "s";
  }

  public abstract static class AbstractSqlViewer implements SqlViewer {
    protected abstract void logTrace(String message);

    @Override
    public void gotConnection(String connectionId, long callNanos) {
      logTrace(connectionId + callNanos(callNanos) + " gotConnection");
    }

    @Override
    public void connectionSetAutoCommit(String connectionId, boolean autoCommit) {
      logTrace(connectionId + " setAutoCommit to " + autoCommit);
    }

    @Override
    public void connectionClose(String connectionId) {
      logTrace(connectionId + " close connection");
    }

    @Override
    public void connectionPrepareStatement(String connectionId, long callNanos, String sql, Object... anotherArgs) {
      logTrace(connectionId + callNanos(callNanos) + " prepareStatement " + sql);
    }

    @Override
    public void statementClose(String connectionId) {
      logTrace(connectionId + " close statement");
    }

    @Override
    public void statementSetParameter(String connectionId, String methodName, int index, Object arg) {
      logTrace(connectionId + " statement " + methodName + " " + index + " " + arg);
    }

    @Override
    public void statementExecute(String connectionId, String methodName, long callNanos) {
      logTrace(connectionId + callNanos(callNanos) + " statement " + methodName);
    }
  }

  private interface HasExtractOriginalConnection {
    Connection extractOriginalConnection_g4h5h34b4h3g2j();
  }

  @SuppressWarnings("unused")
  public static DataSource wrapDataSource(DataSource pool, SqlViewer sqlViewer) {
    return (DataSource) Proxy.newProxyInstance(
        pool.getClass().getClassLoader(),
        new Class[]{DataSource.class},
        new DataSourceInvocationHandler(pool, sqlViewer));
  }

  public static Jdbc wrapJdbc(Jdbc jdbc, SqlViewer sqlViewer) {
    return new Jdbc() {
      @Override
      public <T> T execute(ConnectionCallback<T> connectionCallback) {
        long time1 = System.nanoTime();
        return jdbc.execute(con -> {
          long time2 = System.nanoTime();

          Connection proxiedConnection = (Connection) Proxy.newProxyInstance(
              jdbc.getClass().getClassLoader(),
              new Class[]{Connection.class, HasExtractOriginalConnection.class},
              new ConnectionHandler(con, time2 - time1, sqlViewer, jdbc));

          return connectionCallback.doInConnection(proxiedConnection);
        });
      }
    };
  }

  private static class DataSourceInvocationHandler implements InvocationHandler {

    private final Object originalDataSource;
    private final SqlViewer sqlViewer;

    public DataSourceInvocationHandler(Object originalDataSource, SqlViewer sqlViewer) {
      this.originalDataSource = originalDataSource;
      this.sqlViewer = sqlViewer;
      if (sqlViewer == null) {
        throw new NullPointerException("sqlViewer == null");
      }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

      String methodName = method.getName();
      Class<?>[] parameterTypes = method.getParameterTypes();
      Class<?> returnType = method.getReturnType();

      if (parameterTypes.length == 0) {
        if (methodName.equals("toString")) {
          return "PROXY OF [" + originalDataSource.toString() + "]";
        }
      }

      if (returnType.isAssignableFrom(Connection.class)) {
        long time1 = System.nanoTime();
        Connection connection = (Connection) method.invoke(originalDataSource, args);
        long time2 = System.nanoTime();
        return Proxy.newProxyInstance(
            originalDataSource.getClass().getClassLoader(),
            new Class[]{Connection.class, HasExtractOriginalConnection.class},
            new ConnectionHandler(connection, time2 - time1, sqlViewer, originalDataSource));
      }

      return method.invoke(originalDataSource, args);
    }


  }

  static class ConnectionHandler implements InvocationHandler {

    private final Connection originalConnection;
    private final String connectionId;
    private final SqlViewer sqlViewer;
    private final Object originalDataSource;

    public ConnectionHandler(Connection originalConnection,
                             long callNanos,
                             SqlViewer sqlViewer,
                             Object originalDataSource) {
      this.originalConnection = originalConnection;
      this.sqlViewer = sqlViewer;
      connectionId = extractConnectionId(originalConnection);
      this.originalDataSource = originalDataSource;
      sqlViewer.gotConnection(connectionId, callNanos);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

      String methodName = method.getName();
      Class<?>[] parameterTypes = method.getParameterTypes();

      if (parameterTypes.length == 0) {

        if (methodName.equals("close")) {
          sqlViewer.connectionClose(connectionId);
          return method.invoke(originalConnection, args);
        }

        if (methodName.equals("toString")) {
          return "LOGGING PROXY FOR [" + originalConnection.toString() + "]";
        }


        if (methodName.equals("extractOriginalConnection_g4h5h34b4h3g2j")) {
          return originalConnection;
        }

      } else if (parameterTypes.length == 1) {

        if (methodName.equals("equals")) {
          Object arg0 = args[0];
          if (arg0 instanceof HasExtractOriginalConnection) {
            Connection that = ((HasExtractOriginalConnection) arg0).extractOriginalConnection_g4h5h34b4h3g2j();
            return originalConnection.equals(that);
          }
          return false;
        }

        if (methodName.equals("setAutoCommit")) {
          sqlViewer.connectionSetAutoCommit(connectionId, (boolean) args[0]);
          return method.invoke(originalConnection, args);
        }

        if (methodName.equals("prepareStatement")) {
          long time1 = System.nanoTime();
          Object ret = method.invoke(originalConnection, args);
          long time2 = System.nanoTime();
          sqlViewer.connectionPrepareStatement(connectionId, time2 - time1, (String) args[0]);
          return Proxy.newProxyInstance(originalDataSource.getClass().getClassLoader(),
              new Class[]{PreparedStatement.class},
              new StatementHandler(ret));
        }


      } else if (parameterTypes.length == 3) {

        if (methodName.equals("prepareStatement")) {
          long time1 = System.nanoTime();
          Object ret = method.invoke(originalConnection, args);
          long time2 = System.nanoTime();
          sqlViewer.connectionPrepareStatement(connectionId, time2 - time1, (String) args[0], args[1], args[2]);
          return Proxy.newProxyInstance(originalDataSource.getClass().getClassLoader(),
              new Class[]{PreparedStatement.class},
              new StatementHandler(ret));
        }

      }

      return method.invoke(originalConnection, args);
    }

    class StatementHandler implements InvocationHandler {

      private final Object originalStatement;

      public StatementHandler(Object originalStatement) {
        this.originalStatement = originalStatement;
      }

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();

        if (parameterTypes.length == 0) {

          if (methodName.equals("close")) {
            sqlViewer.statementClose(connectionId);
            return method.invoke(originalStatement, args);
          }

          if (methodName.startsWith("execute")
              || methodName.equals("getUpdateCount")
              || methodName.equals("getResultSet")
          ) {
            long time1 = System.nanoTime();
            Object ret = method.invoke(originalStatement, args);
            long time2 = System.nanoTime();
            sqlViewer.statementExecute(connectionId, methodName, time2 - time1);
            return ret;
          }

        } else if (parameterTypes.length == 2) {

          if (methodName.startsWith("set") && parameterTypes[0] == Integer.TYPE) {
            sqlViewer.statementSetParameter(connectionId, methodName, (Integer) args[0], args[1]);
            return method.invoke(originalStatement, args);
          }

        }

        return method.invoke(originalStatement, args);
      }
    }

  }

  @SuppressWarnings("unused")
  public static Connection extractOriginal(Connection connection) {
    if (connection instanceof HasExtractOriginalConnection) {
      return ((HasExtractOriginalConnection) connection).extractOriginalConnection_g4h5h34b4h3g2j();
    }
    return connection;
  }

  @SuppressWarnings("StringConcatenationInLoop")
  public static String extractConnectionId(Connection connection) {
    String str = "" + System.identityHashCode(connection);
    while (str.length() < 10) {
      str = "0" + str;
    }
    return "CON-" + str;
  }
}
