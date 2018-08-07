package kz.greetgo.security.crypto;

import kz.greetgo.security.util.FileUtil;
import kz.greetgo.util.RND;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;

public class CryptoTest {

  @DataProvider
  Object[][] arraySizeDataProvider() {
    return new Object[][]{
      {10_000}, {20}
    };
  }

  private static final String KEYS_DIR = "build/crypto_test/keys/";

  @Test(dataProvider = "arraySizeDataProvider")
  public void encrypt_decrypt(int arraySize) {
    String prefix = RND.intStr(9);
    File privateKeyFile = new File(KEYS_DIR + "encrypt_decrypt." + prefix + ".private.key");
    File publicKeyFile = new File(KEYS_DIR + "encrypt_decrypt." + prefix + ".public.key");

    {
      Crypto crypto = CryptoBuilder.newBuilder()
        .inFiles(privateKeyFile, publicKeyFile)
        .build();

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
      Crypto crypto = CryptoBuilder.newBuilder()
        .inFiles(privateKeyFile, publicKeyFile)
        .setConfig(new CryptoSourceConfigDefault())
        .build();

      byte[] bytes = RND.byteArray(10_000);

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

  @Test(dataProvider = "arraySizeDataProvider")
  public void encrypt_decrypt_brokenFiles(int arraySize) {
    String prefix = RND.intStr(9);

    File privateKeyFile = new File(KEYS_DIR + "encrypt_decrypt_brokenFiles." + prefix + ".private.key");
    File publicKeyFile = new File(KEYS_DIR + "encrypt_decrypt_brokenFiles." + prefix + ".public.key");

    FileUtil.strToFile("Left content 1", privateKeyFile);
    FileUtil.strToFile("Left content 2", publicKeyFile);

    {
      Crypto crypto = CryptoBuilder.newBuilder()
        .setConfig(new CryptoSourceConfigDefault())
        .inFiles(privateKeyFile, publicKeyFile)
        .build();

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

}
