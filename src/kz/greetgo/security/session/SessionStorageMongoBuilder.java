package kz.greetgo.security.session;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class SessionStorageMongoBuilder {
  private final MongoCollection<Document> collection;

  public SessionStorageMongoBuilder(MongoCollection<Document> collection) {
    this.collection = collection;
  }

  public SessionStorage build() {
    return new SessionStorageMongo(collection);
  }
}
