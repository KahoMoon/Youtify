package Youtube.Logic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlaylistItemsTest {

    PlaylistItems mockPlaylistItems;


    @BeforeEach
    void setUp() {
        mockPlaylistItems = Mockito.mock(PlaylistItems.class);
    }

    @AfterEach
    void tearDown() {
    }


    @Test
    void getTitleDescriptions() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonFromFile = Files.readString(Path.of("C:\\Users\\User\\IdeaProjects\\Youtify\\JSON response 3 maxResults"));
        JsonNode root = objectMapper.readTree(jsonFromFile);
        PlaylistItems playlistItems = new PlaylistItems(root);

        List<TitleDescription> expected = new ArrayList<>();
        expected.add(new TitleDescription("Rain Man feat. Oly - Bring Back The Summer (Not Your Dope Remix)", "Rain Man feat. Oly - Bring Back The Summer (Not Your Dope Remix)\\n? STREAM NOW: https://trapnation.komi.io\\n\\nNot Your Dope\\n? https://soundcloud.com/notyourdope\\n? https://www.facebook.com/nydope\\n\\nRain Man\\n? https://soundcloud.com/omgrainman\\n? http://www.hive.co/l/rainmanig\\n\\n\\n? Copyright Free Music (Safe to Use - No Copyright Claim): https://broke.ffm.to/downloadbroke\\n\\n#notyourdope #rainman #bringbackthesummer #trapnation #trap #edm #trapmusic #trapremix #remix #music #mix #visualizer"));
        expected.add(new TitleDescription("[LYRICS] Rich Edwards - Where I'll Be Waiting (ft. Cozi Zuehlsdorff)", "Please submit any video requests at http://monsterlyrics.co\\n\\nThis is a lyrics video. I am not affiliated with any artists or labels.\\n\\n\\n\\n- - -   DOWNLOADS   - - -\\n\\nDownload the song ?\\nSupport on iTunes: http://monster.cat/1YWpU4p\\nSupport on Beatport: http://monster.cat/1SzaNgU\\nSupport on Bandcamp: http://monster.cat/1rnQB7B\\n\\nDownload the background ?\\nhttp://hangmoon.deviantart.com/art/At-the-island-605767790?q=gallery%3AHangmoon%2F6202821&qo=0\\n\\n\\n\\n- - -   LYRICS   - - -\\n\\nWe don't belong\\nMaking letters out of cowboy sheets\\nWe will be gone\\nBut we're counting stars, they're counting sheep\\n\\nNeed to hold your breath, don't make a sound\\nJust keep running, keep running, keep running\\nIn our heads, we're ten feet off the ground\\nJust keep running, keep running, keep running\\n\\nYou know that place between asleep and awake\\nWhere you can still remember dreams\\nI swear I'll always love you\\nYou know that place between asleep and awake\\nWhere you can still remember dreams\\nI swear I'll always love you\\nAnd that's where I'll be waiting\\n\\nAnd that's where I'll be waiting\\nAnd that's where I'll be waiting\\n\\nFeeling alone\\nNever thought I'd see this place again\\nI'm going on\\nIn a different world, you stay the same\\nI just hold my breath, don't make a sound\\nI'm running, and I keep running, and I keep running\\nCause I can't see you quite as clearly now\\nI'm running and I keep running\\n\\nYou know that place between asleep and awake\\nWhere you can still remember dreams\\nI swear I'll always love you\\nYou know that place between asleep and awake\\nWhere you can still remember dreams\\nI swear I'll always love you\\nAnd that's where I'll be waiting\\n\\nAnd that's where I'll be waiting\\nAnd that's where I'll be waiting\\nAnd that's where I'll be waiting\\nAnd that's where I'll be waiting\\nAnd that's where I'll be waiting\\n\\n\\n\\n- - -   SOCIAL LINKS  - - -\\n\\nMonsterLyrics ?\\nTwitter: http://twitter.com/MonstrousLyrics\\nInstagram: http://instagram.com/MonsterLyrics\\n\\nMonstercat ?\\nYouTube: http://www.youtube.com/user/MonstercatMedia\\nSpotify: http://monster.cat/1hGrCWk\\nFacebook: http://facebook.com/Monstercat\\nTwitter: http://twitter.com/Monstercat\\nInstagram: http://instagram.com/monstercat\\nVine: https://vine.co/monstercat\\nSoundCloud: http://soundcloud.com/Monstercat\\nGoogle+: https://plus.google.com/+Monstercat\\nPodcast: http://www.monstercat.com/podcast/\\n\\nRich Edwards ?\\nFacebook: https://www.facebook.com/richedwardsofficial/\\nTwitter: https://twitter.com/realrichedwards\\nSoundcloud: https://soundcloud.com/Richedwardsofficial\\nInstagram: https://www.instagram.com/richedwardsofficial/\\n\\nCozi Zuehlsdorff ?\\nFacebook: http://on.fb.me/1bccmyZ\\nTwitter: https://twitter.com/CoziZuehlsdorff\\nSoundcloud: https://soundcloud.com/cozi-zuehlsdorff\\nYoutube: https://www.youtube.com/user/CoziZuehlsdorff\\n\\n\\n\\n- - -  CONTACT  - - -\\n\\n* If any artist or label has any issues with my uploads, please contact me at nik@monsterlyrics.co to resolve them."));
        expected.add(new TitleDescription("San Holo - Can't Forget You (ft. The Nicholas)", "San Holo - Can't Forget You (ft. The Nicholas)\\n? STREAM NOW: https://trapnation.komi.io\\n\\nSan Holo\\n? https://soundcloud.com/sanholobeats\\n? https://www.facebook.com/sanholobeats\\n\\nThe Nicholas\\n? https://soundcloud.com/TheNicholasMusic\\n? https://www.facebook.com/thenicholasmusic\\n\\n\\n? Copyright Free Music (Safe to Use - No Copyright Claim): https://broke.ffm.to/downloadbroke\\n\\n#sanholo #sanholobeats #thenicholas #cantforgetyou #trapnation #trap #edm #trapmusic #trapremix #remix #music #mix #visualizer"));


        List<TitleDescription> actual = playlistItems.getTitleDescriptions();
        assertEquals(actual.size(), expected.size());
        for (int i = 0; i < expected.size(); i++) {

            assertEquals(expected.get(i).getTitle(), actual.get(i).getTitle());
            assertEquals(expected.get(i).getDescription(), actual.get(i).getDescription());
        }
    }
}