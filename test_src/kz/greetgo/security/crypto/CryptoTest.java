package kz.greetgo.security.crypto;

import kz.greetgo.db.DbType;
import kz.greetgo.db.Jdbc;
import kz.greetgo.security.crypto.errors.NotEqualsIdFieldLengths;
import kz.greetgo.security.crypto.errors.NotSameIdFieldNames;
import kz.greetgo.security.crypto.errors.UnsupportedDb;
import kz.greetgo.security.factory.JdbcFactory;
import kz.greetgo.util.RND;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;

public class CryptoTest {

  interface CryptoSource {
    Crypto create(String suffix);
  }

  private CryptoSource cryptoSourceInFiles(int keySize) {
    return suffix -> {
      final String keysDir = "build/CryptoTest/keys/";

      File privateKeyFile = new File(keysDir + suffix + ".private.key");
      File publicKeyFile = new File(keysDir + suffix + ".public.key");

      return CryptoBuilder.newBuilder()
        .setKeySize(keySize)
        .inFiles(privateKeyFile, publicKeyFile)
        .setConfig(new CryptoSourceConfigDefault())
        .build();
    };
  }

  JdbcFactory jdbcFactory = new JdbcFactory();

  {
    jdbcFactory.defineDbNameFrom("greetgo_security");
  }

  private CryptoSource onDbInSameTable(DbType dbType, int keySize) {
    return new CryptoSource() {
      @Override
      public String toString() {
        return "SAME TABLE, dbType=" + dbType;
      }

      @Override
      public Crypto create(String suffix) {
        jdbcFactory.dbType = dbType;
        Jdbc jdbc = jdbcFactory.create();

        return CryptoBuilder.newBuilder()
          .setKeySize(keySize)
          .inDb(dbType, jdbc)
          .setConfig(new CryptoSourceConfigDefault())
          .setTableName("crypto_keys_" + suffix)
          .setIdFieldLength(70)
          .setIdFieldName("key_id")
          .setValueFieldName("key_content")
          .setPrivateKeyIdValue("private_key_id")
          .setPublicKeyIdValue("public_key_id")
          .build();
      }
    };
  }

  private CryptoSource onDbInSameTableDiffContent(DbType dbType, int keySize) {
    return new CryptoSource() {
      @Override
      public String toString() {
        return "SAME TABLE, dbType=" + dbType;
      }

      @Override
      public Crypto create(String suffix) {
        jdbcFactory.dbType = dbType;
        Jdbc jdbc = jdbcFactory.create();

        return CryptoBuilder.newBuilder()
          .setKeySize(keySize)
          .inDb(dbType, jdbc)
          .setConfig(new CryptoSourceConfigDefault())
          .setTableName("crypto_keys_" + suffix)
          .setValueFieldNameForPrivateKey("c_private")
          .setValueFieldNameForPublicKey("c_public")
          .build();
      }
    };
  }

  private CryptoSource onDbInDifferentTables(DbType dbType, int keySize) {
    return new CryptoSource() {
      @Override
      public String toString() {
        return "SAME TABLE, dbType=" + dbType + ", keySize=" + keySize;
      }

      @Override
      public Crypto create(String suffix) {
        jdbcFactory.dbType = dbType;
        Jdbc jdbc = jdbcFactory.create();

        return CryptoBuilder.newBuilder()
          .setKeySize(keySize)
          .inDb(dbType, jdbc)
          .setConfig(new CryptoSourceConfigDefault())
          .setTableNameForPrivateKey("crypto_private_" + suffix)
          .setTableNameForPublicKey("crypto_public_" + suffix)
          .build();
      }
    };
  }

  @DataProvider
  Object[][] mainDataProvider() {
    return new Object[][]{
      {cryptoSourceInFiles(1024), 10_000},
      {cryptoSourceInFiles(1024), 20},
      {cryptoSourceInFiles(1024 * 2), 10_000},
      {cryptoSourceInFiles(1024 * 2), 20},

      {onDbInSameTable(DbType.Postgres, 1024), 20},
      {onDbInSameTable(DbType.Oracle, 1024 * 2), 20},

      {onDbInSameTableDiffContent(DbType.Postgres, 1024), 20},
      {onDbInSameTableDiffContent(DbType.Oracle, 1024 * 2), 20},

      {onDbInDifferentTables(DbType.Postgres, 1024), 20},
      {onDbInDifferentTables(DbType.Oracle, 1024 * 2), 20},
    };
  }

  @Test(dataProvider = "mainDataProvider")
  public void encrypt_decrypt(CryptoSource cryptoSource, int arraySize) {
    String suffix = RND.intStr(9);
    {
      Crypto crypto = cryptoSource.create(suffix);

      byte[] bytes = RND.byteArray(arraySize);

      //
      //
      byte[] encryptedBytes = crypto.encrypt(bytes);
      //
      //

      assertThat(encryptedBytes).isNotNull();

      //
      //
      byte[] original = crypto.decrypt(encryptedBytes);
      //
      //

      assertThat(original).describedAs("Checking when keys just created").isEqualTo(bytes);
    }

    {
      Crypto crypto = cryptoSource.create(suffix);

      byte[] bytes = RND.byteArray(arraySize);

      //
      //
      byte[] encryptedBytes = crypto.encrypt(bytes);
      //
      //

      assertThat(encryptedBytes).isNotNull();

      //
      //
      byte[] original = crypto.decrypt(encryptedBytes);
      //
      //

      assertThat(original).describedAs("Checking when keys loaded from files").isEqualTo(bytes);
    }
  }

  @Test(dataProvider = "mainDataProvider")
  public void encrypt_decrypt_brokenFiles(CryptoSource cryptoSource, int arraySize) {
    String suffix = RND.intStr(9);

    {
      Crypto crypto = cryptoSource.create(suffix);

      byte[] bytes = RND.byteArray(arraySize);

      //
      //
      byte[] encryptedBytes = crypto.encrypt(bytes);
      //
      //

      assertThat(encryptedBytes).isNotNull();

      //
      //
      byte[] original = crypto.decrypt(encryptedBytes);
      //
      //

      assertThat(original).describedAs("Checking when keys just restored from broken content").isEqualTo(bytes);
    }
  }

  @Test(expectedExceptions = NotSameIdFieldNames.class)
  public void notSameIdFieldNames() {
    jdbcFactory.dbType = DbType.Postgres;
    Jdbc jdbc = jdbcFactory.create();

    CryptoBuilder.newBuilder()
      .inDb(DbType.Postgres, jdbc)
      .setIdFieldNameForPrivateKey("a")
      .setIdFieldNameForPublicKey("b")
      .build();
  }

  @Test(expectedExceptions = NotEqualsIdFieldLengths.class)
  public void notSameIdFieldLengths() {
    jdbcFactory.dbType = DbType.Postgres;
    Jdbc jdbc = jdbcFactory.create();

    CryptoBuilder.newBuilder()
      .inDb(DbType.Postgres, jdbc)
      .setPrivateIdFieldLength(70)
      .setPublicIdFieldLength(80)
      .build();
  }

  @Test(expectedExceptions = UnsupportedDb.class)
  public void unsupportedDb() {
    jdbcFactory.dbType = DbType.Postgres;
    Jdbc jdbc = jdbcFactory.create();

    CryptoBuilder.newBuilder()
      .inDb(DbType.HSQLDB, jdbc)
      .build();
  }
}
