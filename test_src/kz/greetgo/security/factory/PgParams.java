package kz.greetgo.security.factory;

@SuppressWarnings("FieldCanBeLocal")
public class PgParams {

  private final String host = "localhost";
  private final int port = 12012;
  private final String user = "greetgo_secure";
  private final String password = "111";

  public String user() {
    return user;
  }

  public String password() {
    return password;
  }

  public String url() {
    return "jdbc:postgresql://" + host + ":" + port + "/" + user;
  }
}
