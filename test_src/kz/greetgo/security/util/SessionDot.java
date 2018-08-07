package kz.greetgo.security.util;

import kz.greetgo.security.session.SessionRow;

import java.util.Date;

public class SessionDot {
  public final String id;
  public String token;
  public Object sessionData;
  public Date insertedAt = new Date();
  public Date lastTouchedAt = new Date();

  public SessionDot(String id) {
    this.id = id;
  }

  public SessionRow toRow() {
    return new SessionRow(token, sessionData, insertedAt, lastTouchedAt);
  }
}
