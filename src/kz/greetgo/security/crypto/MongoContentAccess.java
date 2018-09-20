package kz.greetgo.security.crypto;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import kz.greetgo.security.util.Base64Util;
import org.bson.Document;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.eq;
import static kz.greetgo.security.util.MongoUtil.toStr;

public class MongoContentAccess implements ContentAccess {
  private final MongoCollection<Document> collection;
  private final CryptoBuilderKeysInMongo.Names names;

  public MongoContentAccess(MongoCollection<Document> collection, CryptoBuilderKeysInMongo.Names names) {
    this.collection = collection;
    this.names = names;
  }

  @Override
  public byte[] downloadBytes() {

    Document found = collection.find(eq(names.idFieldName, names.idValue)).limit(1).first();

    if (found == null) {
      return null;
    }

    return Base64Util.base64ToBytes(toStr(found.get(names.keyFieldName)));
  }

  @Override
  public void uploadBytes(byte[] bytes) {
    if (bytes == null) {
      removeBytes();
      return;
    }

    String base64 = Base64Util.bytesToBase64(bytes);

    Bson filter = eq(names.idFieldName, names.idValue);

    Document set = new Document();
    set.append(names.keyFieldName, base64);

    Document update = new Document();
    update.append("$set", set);

    UpdateOptions options = new UpdateOptions();
    options.upsert(true);

    collection.updateOne(filter, update, options);
  }

  private void removeBytes() {
    collection.deleteOne(eq(names.idFieldName, names.idValue));
  }

  @Override
  public boolean exists() {
    return null != collection.find(eq(names.idFieldName, names.idValue)).limit(1).first();
  }
}
