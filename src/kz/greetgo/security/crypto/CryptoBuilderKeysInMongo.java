package kz.greetgo.security.crypto;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class CryptoBuilderKeysInMongo {

  private final CryptoBuilder parent;
  private final MongoCollection<Document> forPrivateKey;
  private final MongoCollection<Document> forPublicKey;

  public CryptoBuilderKeysInMongo(CryptoBuilder parent,
                                  MongoCollection<Document> forPrivateKey,
                                  MongoCollection<Document> forPublicKey) {
    this.parent = parent;
    this.forPrivateKey = forPrivateKey;
    this.forPublicKey = forPublicKey;
  }

  static class Names {

    String idFieldName = "id";
    String keyFieldName = "keyContent";

    String idValue;

    @Override
    public String toString() {
      return "Names{" +
        "idFieldName='" + idFieldName + '\'' +
        ", keyFieldName='" + keyFieldName + '\'' +
        ", idValue='" + idValue + '\'' +
        '}';
    }
  }

  private final Names privateKeyNames = new Names();
  private final Names publicKeyNames = new Names();

  {
    privateKeyNames.idValue = "private_key";
    publicKeyNames.idValue = "public_key";
  }

  public CryptoBuilderKeysInMongo setKeysFieldName(String fieldName) {
    return setPrivateKeyFieldName(fieldName).setPublicKeyFieldName(fieldName);
  }

  public CryptoBuilderKeysInMongo setPrivateKeyFieldName(String fieldName) {
    privateKeyNames.keyFieldName = fieldName;
    return this;
  }

  public CryptoBuilderKeysInMongo setPublicKeyFieldName(String fieldName) {
    publicKeyNames.keyFieldName = fieldName;
    return this;
  }

  public CryptoBuilderKeysInMongo setIdFieldName(String fieldName) {
    return setPrivateIdFieldName(fieldName).setPublicIdFieldName(fieldName);
  }

  public CryptoBuilderKeysInMongo setPrivateId(String idValue) {
    privateKeyNames.idValue = idValue;
    return this;
  }

  public CryptoBuilderKeysInMongo setPublicId(String idValue) {
    publicKeyNames.idValue = idValue;
    return this;
  }

  public CryptoBuilderKeysInMongo setPrivateIdFieldName(String fieldName) {
    privateKeyNames.idFieldName = fieldName;
    return this;
  }

  public CryptoBuilderKeysInMongo setPublicIdFieldName(String fieldName) {
    publicKeyNames.idFieldName = fieldName;
    return this;
  }

  public Crypto build() {

    MongoContentAccess privateKeyAccess = new MongoContentAccess(forPrivateKey, privateKeyNames);

    MongoContentAccess publicKeyAccess = new MongoContentAccess(forPublicKey, publicKeyNames);

    return parent.build(privateKeyAccess, publicKeyAccess);
  }
}
