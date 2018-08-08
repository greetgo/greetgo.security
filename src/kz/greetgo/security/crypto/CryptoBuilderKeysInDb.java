package kz.greetgo.security.crypto;

import kz.greetgo.db.DbType;
import kz.greetgo.db.Jdbc;
import kz.greetgo.security.crypto.errors.NotEqualsIdFieldLengths;
import kz.greetgo.security.crypto.errors.NotSameIdFieldNames;
import kz.greetgo.security.crypto.errors.UnsupportedDb;
import kz.greetgo.security.crypto.jdbc.ContentNames;
import kz.greetgo.security.crypto.jdbc.DbDialect;
import kz.greetgo.security.crypto.jdbc.DbDialectOracle;
import kz.greetgo.security.crypto.jdbc.DbDialectPostgres;
import kz.greetgo.security.crypto.jdbc.JdbcContentAccess;
import kz.greetgo.security.crypto.jdbc.create_table.CreateTable;

import java.util.Objects;

public class CryptoBuilderKeysInDb {
  private final CryptoBuilder parent;
  private final DbType dbType;
  private final Jdbc jdbc;

  public CryptoBuilderKeysInDb(CryptoBuilder parent, DbType dbType, Jdbc jdbc) {
    this.parent = parent;
    this.dbType = dbType;
    this.jdbc = jdbc;
  }

  public CryptoBuilderKeysInDb setConfig(CryptoSourceConfig config) {
    parent.setConfig(config);
    return this;
  }

  private static class Names {
    public String tableName = "crypto_keys";
    public String idFieldName = "id";
    public String idValue;
    public String valueFieldName = "content";

    public Names(String idValue) {
      this.idValue = idValue;
    }

    ContentNames fix() {
      return new ContentNames(tableName, idFieldName, idValue, valueFieldName);
    }
  }

  private final Names namesForPrivateKey = new Names("private.key");
  private final Names namesForPublicKey = new Names("public.key");

  public CryptoBuilderKeysInDb setTableName(String tableName) {
    return setTableNameForPrivateKey(tableName).setTableNameForPublicKey(tableName);
  }

  public CryptoBuilderKeysInDb setTableNameForPrivateKey(String tableName) {
    Objects.requireNonNull(tableName);
    namesForPrivateKey.tableName = tableName;
    return this;
  }

  public CryptoBuilderKeysInDb setTableNameForPublicKey(String tableName) {
    Objects.requireNonNull(tableName);
    namesForPublicKey.tableName = tableName;
    return this;
  }

  public CryptoBuilderKeysInDb setIdFieldName(String idFieldName) {
    return setIdFieldNameForPrivateKey(idFieldName).setIdFieldNameForPublicKey(idFieldName);
  }

  public CryptoBuilderKeysInDb setIdFieldNameForPrivateKey(String idFieldName) {
    Objects.requireNonNull(idFieldName);
    namesForPrivateKey.idFieldName = idFieldName;
    return this;
  }

  public CryptoBuilderKeysInDb setIdFieldNameForPublicKey(String idFieldName) {
    Objects.requireNonNull(idFieldName);
    namesForPublicKey.idFieldName = idFieldName;
    return this;
  }

  public CryptoBuilderKeysInDb setPrivateKeyIdValue(String idValue) {
    Objects.requireNonNull(idValue);
    namesForPrivateKey.idValue = idValue;
    return this;
  }

  public CryptoBuilderKeysInDb setPublicKeyIdValue(String idValue) {
    Objects.requireNonNull(idValue);
    namesForPublicKey.idValue = idValue;
    return this;
  }

  public CryptoBuilderKeysInDb setValueFieldName(String valueFieldName) {
    return setValueFieldNameForPrivateKey(valueFieldName).setValueFieldNameForPublicKey(valueFieldName);
  }

  public CryptoBuilderKeysInDb setValueFieldNameForPrivateKey(String valueFieldName) {
    Objects.requireNonNull(valueFieldName);
    namesForPrivateKey.valueFieldName = valueFieldName;
    return this;
  }

  public CryptoBuilderKeysInDb setValueFieldNameForPublicKey(String valueFieldName) {
    Objects.requireNonNull(valueFieldName);
    namesForPublicKey.valueFieldName = valueFieldName;
    return this;
  }

  private int privateIdFieldLength = 50;
  private int publicIdFieldLength = 50;

  public CryptoBuilderKeysInDb setIdFieldLength(int idFieldLength) {
    return setPrivateIdFieldLength(idFieldLength).setPublicIdFieldLength(idFieldLength);
  }

  public CryptoBuilderKeysInDb setPrivateIdFieldLength(int privateIdFieldLength) {
    this.privateIdFieldLength = privateIdFieldLength;
    return this;
  }

  public CryptoBuilderKeysInDb setPublicIdFieldLength(int publicIdFieldLength) {
    this.publicIdFieldLength = publicIdFieldLength;
    return this;
  }

  public Crypto build() {
    DbDialect dialect = calcDialect();

    ContentNames privateKeyNames = namesForPrivateKey.fix();
    ContentNames publicKeyNames = namesForPublicKey.fix();

    final String createTableDDL_privateKey;
    final String createTableDDL_publicKey;

    if (Objects.equals(privateKeyNames.tableName, publicKeyNames.tableName)) {

      if (!Objects.equals(privateKeyNames.idFieldName, publicKeyNames.idFieldName)) {
        throw new NotSameIdFieldNames(privateKeyNames.idFieldName, publicKeyNames.idFieldName);
      }

      if (privateIdFieldLength != publicIdFieldLength) {
        throw new NotEqualsIdFieldLengths(privateIdFieldLength, publicIdFieldLength);
      }

      boolean sameValueFields = Objects.equals(privateKeyNames.valueFieldName, publicKeyNames.valueFieldName);

      CreateTable createTable = new CreateTable(privateKeyNames.tableName);
      createTable
        .newField(privateKeyNames.idFieldName)
        .typeString(privateIdFieldLength)
        .primaryKey();
      createTable
        .newField(privateKeyNames.valueFieldName)
        .setNotNull(sameValueFields)
        .typeBlob();

      if (!sameValueFields) {
        createTable
          .newField(publicKeyNames.valueFieldName)
          .typeBlob();
      }

      createTableDDL_privateKey = createTableDDL_publicKey = dialect.toCreateTableDDL(createTable);
    } else {

      {
        CreateTable createTable = new CreateTable(privateKeyNames.tableName);
        createTable
          .newField(privateKeyNames.idFieldName)
          .primaryKey()
          .typeString(privateIdFieldLength);
        createTable
          .newField(privateKeyNames.valueFieldName)
          .typeBlob()
          .notNull();

        createTableDDL_privateKey = dialect.toCreateTableDDL(createTable);
      }
      {
        CreateTable createTable = new CreateTable(publicKeyNames.tableName);
        createTable
          .newField(publicKeyNames.idFieldName)
          .primaryKey()
          .typeString(publicIdFieldLength);
        createTable
          .newField(publicKeyNames.valueFieldName)
          .typeBlob()
          .notNull();

        createTableDDL_publicKey = dialect.toCreateTableDDL(createTable);
      }
    }

    JdbcContentAccess privateKeyAccess
      = new JdbcContentAccess(jdbc, privateKeyNames, createTableDDL_privateKey, dialect);

    JdbcContentAccess publicKeyAccess
      = new JdbcContentAccess(jdbc, publicKeyNames, createTableDDL_publicKey, dialect);

    return parent.build(privateKeyAccess, publicKeyAccess);
  }

  private DbDialect calcDialect() {
    switch (dbType) {
      case Postgres:
        return new DbDialectPostgres();
      case Oracle:
        return new DbDialectOracle();
      default:
        throw new UnsupportedDb(dbType);
    }
  }
}
