package ka.greetgo.db.factory;

import kz.greetgo.db.DbType;
import kz.greetgo.db.Jdbc;

public class JdbcFactory {
  public DbType dbType = null;
  public String dbName = "test";

  public void defineDbNameFrom(String namePart) {
    dbName = System.getProperty("user.name") + "_" + namePart;
  }

  public Jdbc create() {
    if (dbType == DbType.Postgres) {
      PostgresFactory factory = new PostgresFactory();
      factory.dbName = dbName;
      return factory.create();
    }

    throw new RuntimeException("Unsupported db " + dbType);
  }
}
