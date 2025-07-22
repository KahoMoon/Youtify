package Spotify.Data;

import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class LoginTest {

    Login login;

    @BeforeEach
    void setUp() {
        login = new Login();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void createPlaylist() {
        String playlistId = null;
        try {
            playlistId = login.createPlaylist("Test Playlist 1", false, true, "This is the test description");
            System.out.println(playlistId);
        } catch (Exception e) {
            assertNotNull(playlistId);
        }

    }

//    @Test
//    void getSpotifyTrackURL() {
//    }
}