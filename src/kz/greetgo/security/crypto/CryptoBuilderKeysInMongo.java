package kz.greetgo.security.crypto;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

class CryptoBuilderKeysInMongo {

  static class Names {
    String collection = "key_storage";
    String id = "id";
    String keyContent = "keyContent";

    String idValue;
  }

  private final Names privateKey = new Names();
  private final Names publicKey = new Names();

  {
    privateKey.idValue = "private_key";
    publicKey.idValue = "public_key";
  }

  public CryptoBuilderKeysInMongo(CryptoBuilder parent, MongoCollection<Document> collection) {

  }

  public Crypto build() {
    throw new NotImplementedException();
  }
}
