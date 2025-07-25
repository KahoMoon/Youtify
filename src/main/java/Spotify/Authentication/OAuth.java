package Spotify.Authentication;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class OAuth {
    private static String codeVerifier = null;
    private static String codeChallenge = null;

    public static String generateCodeVerifier() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifierBytes = new byte[32];
        secureRandom.nextBytes(codeVerifierBytes);
        codeVerifier =  Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifierBytes);
        codeChallenge = generateCodeChallenge(codeVerifier);
        return codeVerifier;
    }

    public static String generateCodeChallenge(String codeVerifier) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        byte[] bytes = codeVerifier.getBytes(StandardCharsets.US_ASCII);
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(bytes, 0, bytes.length);
        byte[] digest = messageDigest.digest();
        codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        return codeChallenge;
    }

    public static String getCodeVerifier() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        if (codeVerifier == null) {
            generateCodeVerifier();
            generateCodeChallenge(codeVerifier);
        }
        return codeVerifier;
    }

    public static String getCodeChallenge() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        if (codeVerifier == null) {
            generateCodeVerifier();
        }
        if (codeChallenge == null) {
            generateCodeChallenge(codeVerifier);
        }
        return codeChallenge;
    }
}
