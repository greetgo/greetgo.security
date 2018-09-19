package kz.greetgo.security.session;

import com.mongodb.DBCollection;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class SessionStorageMongoBuilder {
  private final DBCollection collection;

  public SessionStorageMongoBuilder(DBCollection collection) {
    this.collection = collection;
  }

  public SessionStorage build() {
    throw new NotImplementedException();
  }
}
