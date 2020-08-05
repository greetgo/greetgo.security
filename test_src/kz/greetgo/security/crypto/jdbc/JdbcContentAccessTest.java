package kz.greetgo.security.crypto.jdbc;

import kz.greetgo.db.DbType;
import kz.greetgo.db.Jdbc;
import kz.greetgo.security.crypto.ContentAccess;
import kz.greetgo.security.factory.JdbcFactory;
import kz.greetgo.util.RND;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class JdbcContentAccessTest {

  JdbcFactory jdbcFactory = new JdbcFactory();

  @BeforeMethod
  public void createJdbcFactory() {
    jdbcFactory.defineDbNameFrom("session_storage");
  }

  @DataProvider
  private Object[][] dbTypeDataProvider() {
    final String postgresCreateTable = "create table TABLE_NAME (" +
      "  id varchar(50) primary key, " +
      "  content byTea not null" +
      ")";

    final String oracleCreateTable = "create table TABLE_NAME (" +
      "  id varchar2(50) primary key, " +
      "  content blob not null" +
      ")";

    return new Object[][]{
      {DbType.Postgres, new DbDialectPostgres(), postgresCreateTable},
      {DbType.Postgres, new DbDialectPostgres(), postgresCreateTable},
      {DbType.Oracle, new DbDialectOracle(), oracleCreateTable},
      {DbType.Oracle, new DbDialectOracle(), oracleCreateTable},
    };
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void uploadBytes_downloadBytes_exists_delete(DbType dbType, DbDialect dialect, String createTableDdl) {
    jdbcFactory.dbType = dbType;
    Jdbc jdbc = jdbcFactory.create();

    String tableName = "crypto_keys_" + RND.intStr(10);
    createTableDdl = createTableDdl.replaceAll("TABLE_NAME", tableName);

    ContentNames names = new ContentNames(tableName, "id", "private.key", "content");

    ContentAccess contentAccess = new JdbcContentAccess(jdbc, names, createTableDdl, dialect);

    assertThat(contentAccess.exists()).isFalse();

    byte[] content = RND.byteArray(1000);

    //
    //
    contentAccess.uploadBytes(content);
    //
    //

    //
    //
    assertThat(contentAccess.exists()).isTrue();
    //
    //

    //
    //
    byte[] downloadedContent = contentAccess.downloadBytes();
    //
    //

    assertThat(downloadedContent).isEqualTo(content);

    //
    //
    contentAccess.delete();
    //
    //

    //
    //
    assertThat(contentAccess.exists()).isFalse();
    //
    //
  }

  @Test(dataProvider = "dbTypeDataProvider")
  public void uploadBytes_multiThreads(DbType dbType, DbDialect dialect, String createTableDdl) throws Exception {
    jdbcFactory.dbType = dbType;
    Jdbc jdbc = jdbcFactory.create();

    String tableName = "crypto_keys_" + RND.intStr(10);
    createTableDdl = createTableDdl.replaceAll("TABLE_NAME", tableName);

    ContentNames names = new ContentNames(tableName, "id", "private.key", "content");

    ContentAccess contentAccess = new JdbcContentAccess(jdbc, names, createTableDdl, dialect);

    assertThat(contentAccess.exists()).isFalse();

    class TestThread extends Thread {
      RuntimeException error = null;

      @Override
      public void run() {
        try {
          //
          //
          contentAccess.uploadBytes(RND.byteArray(1000));
          //
          //
        } catch (RuntimeException e) {
          error = e;
        }
      }

      void asserts() {
        if (error != null) {
          throw error;
        }
      }
    }

    List<TestThread> threadList = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      threadList.add(new TestThread());
    }

    for (TestThread thread : threadList) {
      thread.start();
    }
    for (TestThread thread : threadList) {
      thread.join();
    }
    for (TestThread thread : threadList) {
      thread.asserts();
    }

    assertThat(contentAccess.exists()).isTrue();
  }
}
