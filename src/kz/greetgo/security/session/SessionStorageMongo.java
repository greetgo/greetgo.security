package kz.greetgo.security.session;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static java.util.Objects.requireNonNull;
import static kz.greetgo.security.session.Serializer.deserializeFromStr;
import static kz.greetgo.security.util.MongoUtil.toDate;
import static kz.greetgo.security.util.MongoUtil.toStr;

class SessionStorageMongo implements SessionStorage {
  private final MongoCollection<Document> collection;

  static class Names {
    String id = "id";
    String sessionData = "sessionData";
    String lastModifiedAt = "lastModifiedAt";
    String insertedAt = "insertedAt";
    String token = "token";
    String actual = "actual";
  }

  private final Names names = new Names();

  @Override
  public String toString() {
    return "SessionStorageMongo{" + collection.getNamespace() + "}";
  }

  public SessionStorageMongo(MongoCollection<Document> collection) {
    this.collection = collection;
  }

  private final AtomicBoolean wasEnsureIndexId = new AtomicBoolean(false);

  private void ensureIndexId() {
    if (wasEnsureIndexId.get()) {
      return;
    }

    BasicDBObject cmd = new BasicDBObject();
    cmd.append(names.id, 1);

    collection.createIndex(cmd);

    wasEnsureIndexId.set(true);
  }

  @Override
  public void insertSession(SessionIdentity identity, Object sessionData) {
    requireNonNull(identity, "identity");
    requireNonNull(identity.id, "identity.id");

    ensureIndexId();

    String sessionDataStr = Serializer.serializeToStr(sessionData);

    Document insert = new Document();
    insert.append(names.id, identity.id);
    insert.append(names.token, identity.token);
    insert.append(names.sessionData, sessionDataStr);
    insert.append(names.insertedAt, new Date());
    insert.append(names.lastModifiedAt, new Date());
    insert.append(names.actual, 1);

    collection.insertOne(insert);
  }

  @Override
  public SessionRow loadSession(String sessionId) {

    ensureIndexId();

    Document found = collection.find(filterById(sessionId)).limit(1).first();

    if (found == null) {
      return null;
    }

    //@formatter:off
    String token       =                      toStr( found.get( names.token           ));
    Object sessionData = deserializeFromStr(  toStr( found.get( names.sessionData     )));
    Date insertedAt    =                     toDate( found.get( names.insertedAt      ));
    Date lastTouchedAt =                     toDate( found.get( names.lastModifiedAt  ));
    //@formatter:on

    return new SessionRow(token, sessionData, insertedAt, lastTouchedAt);
  }

  private Bson filterById(String sessionId) {
    return and(eq(names.id, sessionId), eq(names.actual, 1));
  }

  @Override
  public Date loadLastTouchedAt(String sessionId) {

    ensureIndexId();

    Document found = collection.find(filterById(sessionId))
      .projection(fields(include(names.lastModifiedAt)))
      .limit(1)
      .first();

    if (found == null) {
      return null;
    }

    return toDate(found.get(names.lastModifiedAt));
  }

  @Override
  public boolean zeroSessionAge(String sessionId) {

    ensureIndexId();

    Document values = new Document();
    values.append(names.lastModifiedAt, new Date());

    Document update = new Document();
    update.append("$set", values);

    return collection
      .updateOne(filterById(sessionId), update)
      .getMatchedCount() > 0;
  }

  @Override
  public int removeSessionsOlderThan(int ageInHours) {
    ensureIndexId();

    Document values = new Document();
    values.append(names.actual, 0);

    Document update = new Document();
    update.append("$set", values);

    GregorianCalendar calendar = new GregorianCalendar();
    calendar.add(Calendar.HOUR, -ageInHours);

    Bson filter = and(
      lt(names.lastModifiedAt, calendar.getTime()),
      eq(names.actual, 1)
    );

    return (int) collection
      .updateMany(filter, update)
      .getMatchedCount();
  }

  @Override
  public boolean remove(String sessionId) {
    ensureIndexId();

    Document values = new Document();
    values.append(names.actual, 0);

    Document update = new Document();
    update.append("$set", values);

    Bson filter = and(
      eq(names.id, sessionId),
      eq(names.actual, 1)
    );

    return (int) collection
      .updateMany(filter, update)
      .getMatchedCount() > 0;
  }

  @Override
  public boolean setLastTouchedAt(String sessionId, Date lastTouchedAt) {
    ensureIndexId();

    Document values = new Document();
    values.append(names.lastModifiedAt, lastTouchedAt);

    Document update = new Document();
    update.append("$set", values);

    Bson filter = and(
      eq(names.id, sessionId),
      eq(names.actual, 1)
    );

    return (int) collection
      .updateMany(filter, update)
      .getMatchedCount() > 0;
  }
}
