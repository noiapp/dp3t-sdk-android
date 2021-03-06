package org.dpppt.android.sdk.util;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import org.dpppt.android.sdk.internal.util.Base64Util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;

public class SignatureUtil {

	public static final String HTTP_HEADER_JWS = "signature";
	public static final String HASH_ALGO = "SHA-256";

	private static final String JWS_CLAIM_CONTENT_HASH = "content-hash";

	public static PublicKey getPublicKeyFromBase64(String publicKeyBase64)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		String pubkey = new String(Base64Util.fromBase64(publicKeyBase64));
		byte[] pubkeyRaw = Base64Util.fromBase64(pubkey.replaceAll("-+(BEGIN|END) PUBLIC KEY-+", "").trim());
		return KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(pubkeyRaw));
	}

	public static PublicKey getPublicKeyFromBase64OrThrow(String publicKeyBase64) {
		try {
			return getPublicKeyFromBase64(publicKeyBase64);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
	}

	public static PublicKey getPublicKeyFromCertificateBase64(String certificateBase64) throws CertificateException {
		byte[] certificateBytes = Base64.decode(certificateBase64, Base64.NO_WRAP);
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		X509Certificate certificate =
				(X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certificateBytes));
		return certificate.getPublicKey();
	}

	public static PublicKey getPublicKeyFromCertificateBase64OrThrow(String certificateBase64) {
		try {
			return getPublicKeyFromCertificateBase64(certificateBase64);
		} catch (CertificateException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] getVerifiedContentHash(String jws, PublicKey publicKey) throws SignatureException {
		Jws<Claims> claimsJws = Jwts.parserBuilder()
				.setSigningKey(publicKey)
				.build()
				.parseClaimsJws(jws);
		String hash64 = claimsJws.getBody().get(JWS_CLAIM_CONTENT_HASH, String.class);
		return Base64Util.fromBase64(hash64);
	}

}
