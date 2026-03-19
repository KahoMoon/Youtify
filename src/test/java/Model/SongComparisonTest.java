package Model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

class SongComparisonTest {

    @BeforeEach
    void setUp() throws IOException {
        boolean fileDelete = Files.deleteIfExists(Paths.get("C:\\Users\\Kaho\\IdeaProjects\\Youtify\\Youtify Redflags.txt"));
    }

    @Test
    void testParseYoutubeTitle() throws IOException {
        Set<String> setA = new HashSet<>();
        Set<String> setAA = new HashSet<>();
        Set<String> setB = new HashSet<>();
        Set<String> setBB = new HashSet<>();
        SongComparison.YoutubeTitleSets youtubeTitleSets = new SongComparison.YoutubeTitleSets(setA, setAA, setB, setBB);
        SongComparison songComparison = new SongComparison();
        songComparison.parseYoutubeTitle("Madeon - Pay **No* Mind (Kbubs Remix)", youtubeTitleSets);
        Set<String> setACheck = new HashSet<>(List.of("madeon"));
        Set<String> setBCheck = new HashSet<>(List.of("pay", "no", "mind", "kbubs", "remix"));
        Assertions.assertEquals(youtubeTitleSets.getyTitleFirstOriginal(), setACheck);
        Assertions.assertEquals(youtubeTitleSets.getyTitleSecondOriginal(), setBCheck);

        songComparison.parseYoutubeTitle("グッドバイ -album version- ", new SongComparison.YoutubeTitleSets(new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>()));
    }

    @Test
    void checkTitle() throws IOException {
        File spotifyJSONFile = new File("C:\\Users\\Kaho\\IdeaProjects\\Youtify\\src\\test\\Spotify JSON Response");
        File youtubeJSONFile = new File("C:\\Users\\Kaho\\IdeaProjects\\Youtify\\src\\test\\Youtube JSON Response");

        ObjectMapper spotifyObjectMapper = new ObjectMapper();
        JsonNode spotifyJSONNode = spotifyObjectMapper.readTree(spotifyJSONFile).get("items");

        ObjectMapper youtubeObjectMapper = new ObjectMapper();
        JsonNode youtubeJSONNode = youtubeObjectMapper.readTree(youtubeJSONFile).get("items");

        SongComparison songComparison = new SongComparison();
        Iterator<JsonNode> spotifyJSONIterator = spotifyJSONNode.elements();
        Iterator<JsonNode> youtubeJSONIterator = youtubeJSONNode.elements();
        ArrayList<Double> res = new ArrayList<>();
        while (youtubeJSONIterator.hasNext()) {
            JsonNode youtubeSongJSON = youtubeJSONIterator.next();
            while (spotifyJSONIterator.hasNext()) {
                JsonNode spotifySongJSON = spotifyJSONIterator.next();
                res.add(songComparison.checkTitle(youtubeSongJSON.get("snippet"), spotifySongJSON));
            }
            spotifyJSONIterator = spotifyJSONNode.elements();
        }

        Assertions.assertEquals(100, res.size());
    }
}