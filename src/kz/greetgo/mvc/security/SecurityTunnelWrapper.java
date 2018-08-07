package kz.greetgo.mvc.security;

import kz.greetgo.mvc.interfaces.MvcTrace;
import kz.greetgo.mvc.interfaces.RequestTunnel;
import kz.greetgo.mvc.interfaces.TunnelHandler;
import kz.greetgo.util.events.EventHandler;
import kz.greetgo.util.events.HandlerKiller;

import java.util.Arrays;

import static kz.greetgo.mvc.util.Base64Util.base64ToBytes;
import static kz.greetgo.mvc.util.Base64Util.bytesToBase64;

public class SecurityTunnelWrapper implements TunnelHandler {

  protected final TunnelHandler whatWrapping;
  protected final SecurityProvider provider;
  protected final SessionStorage sessionStorage;
  protected final SecurityCrypto sessionCrypto;
  protected final SecurityCrypto signatureCrypto;

  public static MvcTrace trace;

  public SecurityTunnelWrapper(TunnelHandler whatWrapping, SecurityProvider provider,
                               SessionStorage sessionStorage, SecurityCrypto sessionCrypto, SecurityCrypto signatureCrypto) {
    this.whatWrapping = whatWrapping;
    this.provider = provider;
    this.sessionStorage = sessionStorage;
    this.sessionCrypto = sessionCrypto;
    this.signatureCrypto = signatureCrypto;
  }

  @Override
  public void handleTunnel(final RequestTunnel tunnel) {
    if (trace != null) trace.trace("CP HDBEwehrewh SecurityTunnelWrapper started");
    final String target = tunnel.getTarget();

    if (trace != null) trace.traceInTunnel("start target = " + target, tunnel);

    if (provider.skipSession(target)) {
      if (trace != null) trace.trace("CP djsanjer3 SecurityTunnelWrapper skipSession");
      whatWrapping.handleTunnel(tunnel);
      return;
    }

    final boolean underSecurityUmbrella = provider.isUnderSecurityUmbrella(target);

    if (trace != null) trace.trace("CP sbg45t2 underSecurityUmbrella = " + underSecurityUmbrella);

    final byte[] bytesInStorage;

    {
      final String sessionBase64 = tunnel.cookies().name(provider.cookieKeySession()).value();
      if (trace != null) trace.trace("CP bsWyw312ger READ COOKIE "
        + provider.cookieKeySession() + " = " + sessionBase64);
      byte[] bytes = base64ToBytes(sessionBase64);

      if (trace != null) trace.trace("CP HEBWJD bytes " + (bytes == null ? "is null" : " is not null"));

      if (sessionCrypto != null) {
        if (trace != null) trace.trace("CP GRHWVe sessionCrypto != null");
        bytes = sessionCrypto.decrypt(bytes);
      }

      if (underSecurityUmbrella && bytes != null && signatureCrypto != null) {
        if (trace != null) trace.trace("CP QTTSFrt ");
        String signatureBase64 = tunnel.cookies().name(provider.cookieKeySignature()).value();
        if (trace != null) trace.trace("CP h2hrhbrfer READ COOKIE " + provider.cookieKeySignature()
          + " = " + signatureBase64);
        byte[] signature = base64ToBytes(signatureBase64);
        if (!signatureCrypto.verifySignature(bytes, signature)) {
          if (trace != null) trace.trace("CP QKMRTBG bytes := null");
          bytes = null;
        }
      }

      if (trace != null) trace.trace("CP hrebtrhet sessionStorage.setSessionBytes(...)");
      sessionStorage.setSessionBytes(bytesInStorage = bytes);
    }

    final EventHandler writeSessionToCookies = new EventHandler() {
      boolean performed = false;

      @Override
      public void handle() {
        if (trace != null) trace.trace("CP ns63v5gGD7 Started writeSessionToCookies");
        if (performed) return;
        performed = true;

        if (trace != null) trace.trace("CP h6h34vyt");

        byte[] bytes = sessionStorage.getSessionBytes();
        if (trace != null) trace.trace("CP 543nj654 "
          + (bytes == null ? "bytes is null" : "bytes is not null"));

        if (Arrays.equals(bytesInStorage, bytes)) {
          if (trace != null) trace.trace("CP krmet54 EQUALS");
          return;
        }

        if (trace != null) trace.trace("CP ktmreyr");

        if (bytes == null) {
          cleanSecurityCookies(tunnel);
        } else {

          if (trace != null) trace.trace("CP thbrejhby bytes != null");

          if (signatureCrypto != null) {
            byte[] signature = signatureCrypto.sign(bytes);
            final String signatureBase64 = bytesToBase64(signature);
            if (trace != null) trace.trace("CP tytgfyr SET COOKIE " + provider.cookieKeySignature()
              + " = " + signatureBase64);
            saveSignatureToCookies(tunnel, signatureBase64);
          }

          if (sessionCrypto != null) {
            if (trace != null) trace.trace("CP yfc56g34kjd sessionCrypto != null");
            bytes = sessionCrypto.encrypt(bytes);
          }

          final String bytesBase64 = bytesToBase64(bytes);
          if (trace != null) trace.trace("CP vv4t5v43t SET COOKIE " + provider.cookieKeySession()
            + " = " + bytesBase64);
          saveSessionToCookies(tunnel, bytesBase64);
        }

      }
    };

    final HandlerKiller handlerKiller = tunnel.eventBeforeCompleteHeaders()
      .addEventHandler(writeSessionToCookies);

    if (trace != null) trace.trace("CP bgeneju478r");

    try {

      if (!underSecurityUmbrella) {

        if (trace != null) trace.trace("CP nsbhryhfbv");

        whatWrapping.handleTunnel(tunnel);

        if (trace != null) trace.trace("CP jen4676v4g");

      } else {

        if (sessionStorage.getSessionBytes() == null) {
          String reference = provider.redirectOnSecurityError(target);
          if (trace != null) trace.trace("CP hytrjtreh redirect to " + reference);
          tunnel.sendRedirect(reference);
        } else {
          if (trace != null) trace.trace("CP fbdvfgd EXECUTE");
          whatWrapping.handleTunnel(tunnel);
        }

      }

    } finally {
      handlerKiller.kill();
      writeSessionToCookies.handle();
      if (trace != null) trace.trace("CP fjewhtb FINALLY");
    }

  }

  protected void cleanSecurityCookies(RequestTunnel tunnel) {
    if (trace != null) trace.trace("CP uyu76gfh4 DELETE COOKIE " + provider.cookieKeySession());
    tunnel.cookies().forName(provider.cookieKeySession())
      .path("/")
      .remove();

    if (trace != null) trace.trace("CP wsS676Sdd DELETE COOKIE " + provider.cookieKeySignature());
    tunnel.cookies().forName(provider.cookieKeySignature())
      .path("/")
      .remove();
  }

  protected void saveSessionToCookies(RequestTunnel tunnel, String sessionBase64) {
    tunnel.cookies().forName(provider.cookieKeySession())
      .path("/")
      .saveValue(sessionBase64);
  }

  protected void saveSignatureToCookies(RequestTunnel tunnel, String signatureBase64) {
    tunnel.cookies().forName(provider.cookieKeySignature())
      .path("/")
      .saveValue(signatureBase64);
  }
}
