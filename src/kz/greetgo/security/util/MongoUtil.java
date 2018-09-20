package kz.greetgo.security.util;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Date;

public class MongoUtil {
  public static String toStr(Object objectValue) {
    if (objectValue == null) {
      return null;
    }
    if (objectValue instanceof String) {
      return (String) objectValue;
    }
    throw new IllegalArgumentException("Cannot convert to string the value of "
      + objectValue.getClass() + " = " + objectValue);
  }

  public static Date toDate(Object objectValue) {
    if (objectValue == null) {
      return null;
    }
    if (objectValue instanceof Date) {
      return (Date) objectValue;
    }
    throw new IllegalArgumentException("Cannot convert to Date the value of "
      + objectValue.getClass() + " = " + objectValue);
  }
}

