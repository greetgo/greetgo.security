package ka.greetgo.db.session;

import java.util.Date;

public class SessionDot {
  final String id;
  String token;
  Object sessionData;
  Date insertedAt = new Date();
  Date lastTouchedAt = new Date();

  SessionDot(String id) {
    this.id = id;
  }

  public SessionRow toRow() {
    return new SessionRow(token, sessionData, insertedAt, lastTouchedAt);
  }
}
