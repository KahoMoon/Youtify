package Spotify.Authentication;

import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERefreshRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERequest;
import se.michaelthelin.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.*;


public class AuthController {
    private static final URI redirectUri = SpotifyHttpManager.makeUri("http://localhost:8080");

    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(Keys.getId())
            .setRedirectUri(redirectUri)
            .build();

    private static String userId = "";

    public AuthController() {

        while (true) {
            try {
                //request authorization URL using code verifier
                String codeVerifier = OAuth.generateCodeVerifier();
                String codeChallenge = OAuth.getCodeChallenge();
                final AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi
                        .authorizationCodePKCEUri(codeChallenge)
                        .scope("playlist-modify-private, playlist-modify-public")
                        .show_dialog(true)
                        .build();
                final URI authorizationUri = authorizationCodeUriRequest.execute();

                //get response URL
                System.out.println("URI: " + authorizationUri.toString());
                System.out.println("Enter response URL: ");
                Scanner scanner = new Scanner(System.in);
                URI responseURL = URI.create(scanner.nextLine());
                scanner.close();

                //get response code map
                Map<String, String> queryMap = getQueryMap(responseURL.getQuery());

                //check for errors
                if (queryMap.containsKey("error")) {
                    System.out.println("Error: " + queryMap.get("error"));
                    continue;
                }
                String code = queryMap.get("code");

                final AuthorizationCodePKCERequest authorizationCodePKCERequest = spotifyApi
                        .authorizationCodePKCE(Keys.getId(), code, codeVerifier, redirectUri)
                        .build();
                final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodePKCERequest.execute();
                spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
                spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());

                getUserIdFromSpotify();
            } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (SpotifyWebApiException | IOException | ParseException e) {
                System.out.println("Error: " + e);
            }
            break;
        }

        System.out.println("Login Successful");
    }

    private static Map<String, String> getQueryMap(String query) {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<>();

        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }

    private void refreshAccessToken() throws IOException, ParseException, SpotifyWebApiException {
        AuthorizationCodePKCERefreshRequest authorizationCodePKCERefreshRequest = spotifyApi.authorizationCodePKCERefresh(Keys.getId(), spotifyApi.getRefreshToken()).build();

        authorizationCodePKCERefreshRequest.execute();
    }

    private void getUserIdFromSpotify() throws IOException, ParseException, SpotifyWebApiException {
        GetCurrentUsersProfileRequest getCurrentUsersProfileRequest = spotifyApi.getCurrentUsersProfile()
                .build();
        User user = getCurrentUsersProfileRequest.execute();
        userId = user.getId();
    }

    public static SpotifyApi getSpotifyApi() {
        return spotifyApi;
    }

    public static String getUserId() {
        return userId;
    }
}
