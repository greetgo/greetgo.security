package kz.greetgo.security.crypto.errors;

public class NotEqualsIdFieldLengths extends RuntimeException {
  public NotEqualsIdFieldLengths(int privateKeyIdFieldLength, int publicKeyIdFieldLength) {
    super("If table same for both keys, then id field lengths MUST be equals,\n" +
      "    but privateKey.idField length = '" + privateKeyIdFieldLength + "'\n" +
      "    and publicKey.idField length = '" + publicKeyIdFieldLength + "'");
  }
}
