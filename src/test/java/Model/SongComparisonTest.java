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

    @Test
    void testParseYoutubeTitle() throws IOException {
        boolean fileDelete = Files.deleteIfExists(Paths.get("C:\\Users\\Kaho\\IdeaProjects\\Youtify\\Youtify Redflags.txt"));
        System.out.println(fileDelete);
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

//        setA = new HashSet<>();
//        setB = new HashSet<>();
//        songComparison.parseYoutubeTitle("Juice WRLD - \"Legends\" (Official Audio)", youtubeTitleSets);
//        setACheck = new HashSet<>(List.of("juice", "wrld"));
//        setBCheck = new HashSet<>(List.of("legends", "official", "audio"));
//        Assertions.assertEquals(setA, setACheck);
//        Assertions.assertEquals(setB, setBCheck);

        songComparison.parseYoutubeTitle("グッドバイ -album version- ", new SongComparison.YoutubeTitleSets(new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>()));
    }

}