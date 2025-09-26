package Spotify.Data;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERefreshRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERequest;
import se.michaelthelin.spotify.requests.data.playlists.CreatePlaylistRequest;
import se.michaelthelin.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

import java.io.IOException;
import java.net.URI;
import java.security.SecureRandom;
import java.util.*;

public class Login {
    private static final URI redirectUri = SpotifyHttpManager.makeUri("http://localhost:8080");
    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder().setClientId(Keys.CLIENT_ID.getId()).setRedirectUri(redirectUri).build();
    private static String codeVerifier = null;
    private static final AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodePKCEUri(codeVerifier).scope("playlist-modify-private, playlist-modify-public").show_dialog(false).build();
    private static String code = "";
    private static final AuthorizationCodePKCERequest authorizationCodePKCERequest = spotifyApi.authorizationCodePKCE(code, codeVerifier).build();

    private String userId = "";

    public Login() throws IOException, ParseException, SpotifyWebApiException {
        //create the URL for requesting authorization code
        final URI uri = authorizationCodeUriRequest.execute();
        //generate random verifier code for oAuth
        codeVerifier = generateCodeVerifier();
        System.out.println("URI: " + uri.toString());

        //http://localhost:8080/?code=AQAVUA6PpWR8-za4tqEvMkDA_q_OIMFcx8_GnpiVTRt-zntTqlLd07ZpaUDydjcoD4QXG9S9XlPKsx_QbabFtpP5ZzhPioDAVY4UeONq0Bh7b9A_As_WKzxmD1qnwJTM-IZASgggjkQMELG5bnwhze3etkoFmYixILQyAGoQStMQAG7H_AymmdhL1ynaRhY_liVjXS94LdQjZf-y3beUBcMMXJU9HGkWVp2uNklNYd7uG6csoMc
        System.out.println("Enter the response URL: ");
        Scanner scanner = new Scanner(System.in);
        URI responseURL = URI.create(scanner.nextLine());
        scanner.close();
        //parse response url
        Map<String, String> queryMap = getQueryMap(responseURL.getQuery());

        //check if response url corresponds to success or failure
        if (queryMap.containsKey("code")) {
            code = queryMap.get("code");
        } else {
            System.out.println("Error: " + queryMap);
        }

        try {
            final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodePKCERequest.execute();
            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }



        getUserId();
    }

    private static String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifierBytes = new byte[32];
        secureRandom.nextBytes(codeVerifierBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifierBytes);
    }

    public static Map<String, String> getQueryMap(String query) {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();

        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }

    private void refreshAccessToken() throws IOException, ParseException, SpotifyWebApiException {
        AuthorizationCodePKCERefreshRequest authorizationCodePKCERefreshRequest = spotifyApi.authorizationCodePKCERefresh(Keys.CLIENT_ID.getId(), spotifyApi.getRefreshToken()).build();

        authorizationCodePKCERefreshRequest.execute();
    }

    public String createPlaylist(String name, boolean collaborative, boolean publicPrivate, String description) throws IOException, ParseException, SpotifyWebApiException {
        CreatePlaylistRequest createPlaylistRequest = spotifyApi.createPlaylist(userId, name).collaborative(collaborative).public_(publicPrivate).description(description).build();
        createPlaylistRequest.execute();


        List<NameValuePair> response = createPlaylistRequest.getBodyParameters();
        System.out.println(response.get(5).getName());
        return response.get(5).getValue();
    }

    public String[] getSpotifyTrackURL() {
        return null;
    }

    private void getUserId() throws IOException, ParseException, SpotifyWebApiException {
        GetCurrentUsersProfileRequest getCurrentUsersProfileRequest = spotifyApi.getCurrentUsersProfile()
                .build();
        User user = getCurrentUsersProfileRequest.execute();
        this.userId = user.getId();
    }
}
