package kz.greetgo.security.crypto.errors;

public class NotSameIdFieldNames extends RuntimeException {
  public NotSameIdFieldNames(String privateKeyIdFieldName, String publicKeyIdFieldName) {
    super("If table same for both keys, then id field names MUST be same,\n" +
      "    but privateKey.idFieldName = '" + privateKeyIdFieldName + "'\n" +
      "    and publicKey.idFieldName = '" + publicKeyIdFieldName + "'");
  }
}
