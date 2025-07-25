package Spotify.Data;

import Spotify.Authentication.AuthController;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.requests.data.playlists.CreatePlaylistRequest;

import java.io.IOException;
import java.util.List;

import static Spotify.Authentication.AuthController.getUserId;


public class Playlist {

    SpotifyApi spotifyApi = AuthController.getSpotifyApi();

    public String createPlaylist(String name, boolean collaborative, boolean publicPrivate, String description) throws IOException, ParseException, SpotifyWebApiException {
        CreatePlaylistRequest createPlaylistRequest = spotifyApi.createPlaylist(getUserId(), name).collaborative(collaborative).public_(publicPrivate).description(description).build();
        createPlaylistRequest.execute();


        List<NameValuePair> response = createPlaylistRequest.getBodyParameters();
        System.out.println(response.get(5).getName());
        return response.get(5).getValue();
    }
}
