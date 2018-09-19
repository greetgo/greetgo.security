package kz.greetgo.security.util;

import kz.greetgo.security.crypto.errors.SqlWrapper;
import kz.greetgo.security.errors.LeftPostgresJdbcUrl;
import org.testng.ITestResult;

public class SkipListener extends ITestListenerAbstract {

  boolean printedNoOraDriver = false;
  boolean printedNoPgEnvAccess = false;

  @Override
  public void onTestFailure(ITestResult result) {
    if (noOraDriver(result)) { return; }
    if (noPostgresAdminEnv(result)) { return; }
  }

  private boolean noPostgresAdminEnv(ITestResult result) {
    if (!isPostgresEnvError(result.getThrowable())) { return false; }
    result.setStatus(ITestResult.SKIP);
    if (printedNoPgEnvAccess) { return true; }
    printedNoPgEnvAccess = true;

    printNote("" +
      "********************************************************************************************************\n" +
      "\n" +
      "    Some test skipped because no access to PostgreSQL DB\n\n" +
      "    To access to PostgreSQL DB you can add following environment variables:\n\n" +
      "        PG_ADMIN_URL=jdbc:postgresql://localhost:5432/postgres\n" +
      "        PG_ADMIN_USERID=postgres\n" +
      "        PG_ADMIN_PASSWORD=secret\n" +
      "\n" +
      "********************************************************************************************************\n" +
      "");

    return true;
  }

  private boolean isPostgresEnvError(Throwable error) {
    if (error instanceof LeftPostgresJdbcUrl) {
      return true;
    }
    if (error.getCause() instanceof LeftPostgresJdbcUrl) {
      return true;
    }
    if (error.getCause() != null && error.getCause().getCause() instanceof LeftPostgresJdbcUrl) {
      return true;
    }

    {
      SqlWrapper sqlWrapper = extractSqlWrapper(error);
      if (sqlWrapper != null) {

        if ("08001".equals(sqlWrapper.sqlState)) {
          return true;
        }

        return false;
      }
    }

    return false;
  }

  private SqlWrapper extractSqlWrapper(Throwable error) {
    if (error instanceof SqlWrapper) {
      return (SqlWrapper) error;
    }
    if (error.getCause() instanceof SqlWrapper) {
      return (SqlWrapper) error.getCause();
    }
    if (error.getCause() != null && error.getCause().getCause() instanceof SqlWrapper) {
      return (SqlWrapper) error.getCause().getCause();
    }
    return null;
  }

  private boolean noOraDriver(ITestResult result) {
    String notFoundClass = getNotFoundClass(result.getThrowable());
    if (!"oracle.jdbc.driver.OracleDriver".equals(notFoundClass)) { return false; }
    result.setStatus(ITestResult.SKIP);
    if (printedNoOraDriver) { return true; }
    printedNoOraDriver = true;

    String pd = System.getProperty("PROJECT_DIR");
    printNote("" +
      "********************************************************************************************************\n" +
      "\n" +
      "    Some tests skipped because there is no oracle driver.\n\n" +
      "    To run these tests you can put oracle driver into directory:\n\n" +
      "        " + pd + "/lib_oracle_driver'\n\n" +
      "    Oracle driver is a jar-file with name ojdbc6.jar or ojdbc8.jar\n\n" +
      "    You can download oracle driver from site: \n\n" +
      "       http://www.oracle.com/technetwork/database/application-development/jdbc/downloads/index.html\n" +
      "     \n" +
      "********************************************************************************************************\n" +
      "");
    return true;
  }

  private String getNotFoundClass(Throwable throwable) {

    for (int i = 0; i < 5; i++) {
      if (throwable == null) { return null; }
      if (throwable instanceof ClassNotFoundException) {
        return throwable.getMessage();
      }
      throwable = throwable.getCause();
    }

    return null;
  }
}
