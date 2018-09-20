package kz.greetgo.security.crypto;

import com.mongodb.client.MongoCollection;
import kz.greetgo.db.DbType;
import kz.greetgo.db.Jdbc;
import kz.greetgo.security.crypto.errors.NotEqualsIdFieldLengths;
import kz.greetgo.security.crypto.errors.NotSameIdFieldNames;
import kz.greetgo.security.crypto.errors.UnsupportedDb;
import kz.greetgo.security.factory.JdbcFactory;
import kz.greetgo.security.factory.OracleFactory;
import kz.greetgo.security.util.SkipListener;
import kz.greetgo.security.util.TestMongoUtil;
import kz.greetgo.util.RND;
import org.bson.Document;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static kz.greetgo.security.SecurityBuilders.newCryptoBuilder;
import static kz.greetgo.security.util.TestMongoUtil.connectGetCollection;
import static org.fest.assertions.api.Assertions.assertThat;

@Listeners(SkipListener.class)
public class CryptoTest {

  interface CryptoSource {
    Crypto create(String suffix);
  }

  private CryptoSource cryptoSourceInFiles(int keySize) {
    return suffix -> {
      final String keysDir = "build/test_data/CryptoTest/keys/";

      File privateKeyFile = new File(keysDir + suffix + ".private.key");
      File publicKeyFile = new File(keysDir + suffix + ".public.key");

      return newCryptoBuilder()
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

        return newCryptoBuilder()
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

        return newCryptoBuilder()
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

  private CryptoSource onMongoInSameCollection(int keySize) {
    return new CryptoSource() {

      @Override
      public String toString() {
        return "SAME TABLE MONGO, keySize = " + keySize;
      }

      @Override
      public Crypto create(String suffix) {

        if (!TestMongoUtil.hasMongodb()) {
          throw new SkipException("MongoDB cannot be accessed");
        }

        MongoCollection<Document> collection = connectGetCollection(System.getProperty("user.name") + "_crypto");

        return CryptoBuilder.newBuilder()
          .setKeySize(keySize)
          .setConfig(new CryptoSourceConfigDefault())
          .inMongo(collection)
          .build()
          ;

      }
    };
  }

  @DataProvider
  Object[][] mainDataProvider() {
    List<Object[]> list = new ArrayList<>();
    list.add(new Object[]{cryptoSourceInFiles(1024), 10_000});
    list.add(new Object[]{cryptoSourceInFiles(1024), 20});
    list.add(new Object[]{cryptoSourceInFiles(1024 * 2), 10_000});
    list.add(new Object[]{cryptoSourceInFiles(1024 * 2), 20});

    list.add(new Object[]{onDbInSameTable(DbType.Postgres, 1024), 20});
    if (OracleFactory.hasOracleDriver()) {
      list.add(new Object[]{onDbInSameTable(DbType.Oracle, 1024 * 2), 20});
    }

    list.add(new Object[]{onDbInSameTableDiffContent(DbType.Postgres, 1024), 20});
    if (OracleFactory.hasOracleDriver()) {
      list.add(new Object[]{onDbInSameTableDiffContent(DbType.Oracle, 1024 * 2), 20});
    }

    list.add(new Object[]{onDbInDifferentTables(DbType.Postgres, 1024), 20});
    if (OracleFactory.hasOracleDriver()) {
      list.add(new Object[]{onDbInDifferentTables(DbType.Oracle, 1024 * 2), 20});
    }

    list.add(new Object[]{onMongoInSameCollection(1024), 20});
    list.add(new Object[]{onMongoInSameCollection(1024 * 2), 20});
    list.add(new Object[]{onMongoInSameCollection(1024), 20_000});
    list.add(new Object[]{onMongoInSameCollection(1024 * 2), 20_000});

    return list.toArray(new Object[list.size()][]);
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

    newCryptoBuilder()
      .inDb(DbType.Postgres, jdbc)
      .setIdFieldNameForPrivateKey("a")
      .setIdFieldNameForPublicKey("b")
      .build();
  }

  @Test(expectedExceptions = NotEqualsIdFieldLengths.class)
  public void notSameIdFieldLengths() {
    jdbcFactory.dbType = DbType.Postgres;
    Jdbc jdbc = jdbcFactory.create();

    newCryptoBuilder()
      .inDb(DbType.Postgres, jdbc)
      .setPrivateIdFieldLength(70)
      .setPublicIdFieldLength(80)
      .build();
  }

  @Test(expectedExceptions = UnsupportedDb.class)
  public void unsupportedDb() {
    jdbcFactory.dbType = DbType.Postgres;
    Jdbc jdbc = jdbcFactory.create();

    newCryptoBuilder()
      .inDb(DbType.HSQLDB, jdbc)
      .build();
  }

  @Test(dataProvider = "mainDataProvider")
  public void sign_verifySignature(CryptoSource cryptoSource, int arraySize) {
    String suffix = RND.intStr(9);

    Crypto crypto = cryptoSource.create(suffix);

    byte[] bytes = RND.byteArray(arraySize);

    //
    //
    byte[] signature = crypto.sign(bytes);
    //
    //

    assertThat(signature).isNotNull();

    //
    //
    boolean verificationResult = crypto.verifySignature(bytes, signature);
    //
    //

    assertThat(verificationResult).isTrue();

    bytes[3] = (byte) (bytes[3] ^ 1);//change one bit

    //
    //
    boolean verificationResult2 = crypto.verifySignature(bytes, signature);
    //
    //

    assertThat(verificationResult2).isFalse();
  }

  @Test(dataProvider = "mainDataProvider")
  public void rnd(CryptoSource cryptoSource, int arraySize) {
    String suffix = RND.intStr(9);

    Crypto crypto = cryptoSource.create(suffix);

    SecureRandom rnd = crypto.rnd();

    long[] longs = rnd.longs().limit(arraySize * 100).toArray();
    Arrays.sort(longs);

    for (int i = 1; i < longs.length; i++) {
      assertThat(longs[i - 1]).isNotEqualTo(longs[i]);
    }
  }
}
