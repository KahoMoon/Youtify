package Model;
import com.anyascii.AnyAscii;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.*;

public class SongComparison {

    /**Holds the separated sections of a Youtube title in its original form and an unhomoglyphed form*/
    public static class YoutubeTitleSets {
        Set<String> yTitleFirstOriginal;
        Set<String> yTitleFirstUnhomoglyph;

        public Set<String> getyTitleFirstOriginal() {
            return yTitleFirstOriginal;
        }

        public void setyTitleFirstOriginal(Set<String> yTitleFirstOriginal) {
            this.yTitleFirstOriginal = yTitleFirstOriginal;
        }

        public Set<String> getyTitleFirstUnhomoglyph() {
            return yTitleFirstUnhomoglyph;
        }

        public void setyTitleFirstUnhomoglyph(Set<String> yTitleFirstUnhomoglyph) {
            this.yTitleFirstUnhomoglyph = yTitleFirstUnhomoglyph;
        }

        public Set<String> getyTitleSecondOriginal() {
            return yTitleSecondOriginal;
        }

        public void setyTitleSecondOriginal(Set<String> yTitleSecondOriginal) {
            this.yTitleSecondOriginal = yTitleSecondOriginal;
        }

        public Set<String> getyTitleSecondUnhomoglyph() {
            return yTitleSecondUnhomoglyph;
        }

        public void setyTitleSecondUnhomoglyph(Set<String> yTitleSecondUnhomoglyph) {
            this.yTitleSecondUnhomoglyph = yTitleSecondUnhomoglyph;
        }

        Set<String> yTitleSecondOriginal;
        Set<String> yTitleSecondUnhomoglyph;

        YoutubeTitleSets() {
            this.yTitleFirstOriginal = new HashSet<>();
            this.yTitleFirstUnhomoglyph = new HashSet<>();
            this.yTitleSecondOriginal = new HashSet<>();
            this.yTitleSecondUnhomoglyph = new HashSet<>();
        }

        YoutubeTitleSets(Set<String> yTitleFirstOriginal, Set<String> yTitleFirstUnhomoglyph, Set<String> yTitleSecondOriginal, Set<String> yTitleSecondUnhomoglyph) {
            this.yTitleFirstOriginal = yTitleFirstOriginal;
            this.yTitleFirstUnhomoglyph = yTitleFirstUnhomoglyph;
            this.yTitleSecondOriginal = yTitleSecondOriginal;
            this.yTitleSecondUnhomoglyph = yTitleSecondUnhomoglyph;
        }

        void combine() {
            this.yTitleFirstOriginal.addAll(this.yTitleSecondOriginal);
            this.yTitleFirstUnhomoglyph.addAll(this.yTitleSecondUnhomoglyph);
            this.yTitleSecondOriginal = new HashSet<>();
            this.yTitleSecondUnhomoglyph = new HashSet<>();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            YoutubeTitleSets that = (YoutubeTitleSets) o;
            return yTitleFirstOriginal.equals(that.yTitleFirstOriginal) && yTitleFirstUnhomoglyph.equals(that.yTitleFirstUnhomoglyph) && yTitleSecondOriginal.equals(that.yTitleSecondOriginal) && yTitleSecondUnhomoglyph.equals(that.yTitleSecondUnhomoglyph);
        }
    }

    File redflags;
    int CONFIDENCEINTERVAL = 8;

    //RED FLAG ANY YOUTUBE VIDEO THAT IS LONGER THAN 5 minutes
    final String[] redFlag = {"cover", "remix", "flip", "instrumental", "live", "acoustic", "ver", "version", "mashup", "edit", "slowed", "doomer", "nightcore"};
    static double matchProbability = 1.0;

    public SongComparison() {
        while (true) {
            try {
                redflags = new File("Youtify Redflags.txt");
                if (redflags.createNewFile()) {
                    break;
                }
            } catch (FileAlreadyExistsException e) {
                System.out.println("The file already exists.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //if yt title contains poster name, cancel one of them out
    public boolean compare(JsonNode youtubeSongJSON, JsonNode spotifySongJSON) {
        //String[] youtubeSongInfoArr = youtubeSongInfo.split("[^\\w']+");
        //String[] youtubeSongInfoArr = youtubeSongInfo.split("[^a-zA-Z0-9]");

        double res = 1.0;

        res *= checkTitle(youtubeSongJSON, spotifySongJSON);
        if (res < CONFIDENCEINTERVAL) {
            return false;
        }

        //res *= checkArtist(JsonNode youtubeSongJSON, JsonNode spotifySongJSON);
        if (res < CONFIDENCEINTERVAL) {
            return false;
        }

        //res *= checkLength(JsonNode youtubeSongJSON, JsonNode spotifySongJSON);
        if (res < CONFIDENCEINTERVAL) {
            return false;
        }

        return true;
    }

    /*
    * @param youtubeSongJSON snippet JSON
    * @param spotifySongJSON items JSON*/
    public double checkTitle(JsonNode youtubeSongJSON, JsonNode spotifySongJSON) {
        double res = 1.0;

        String sTitle = spotifySongJSON.get("name").asText();
        Set<String> sTitleSet = new LinkedHashSet<>(Arrays.asList(sTitle.split("\\\\s+")));

        String yTitle = youtubeSongJSON.get("title").asText();
        LinkedHashSet<String> yTitleSet = new LinkedHashSet<>();


        return res;
    }

    public void parseSpotifyTitle(String spotifyTitle, Set<String> spotifyTitleSet) {
        String[] spotifyTitleSplit = spotifyTitle.split("\\s+");
        for (String word : spotifyTitleSplit) {
            if (word.matches("^-?\\d+$")) {
                spotifyTitleSet.add(word.toLowerCase());
                continue;
            }
            word = word.replaceFirst("^[^a-zA-Z]+", "");
            word = word.replaceAll("[^a-zA-Z]+$", "");
            if (word.isEmpty()) {
                continue;
            }
            spotifyTitleSet.add(word.toLowerCase());
        }
    }

    /**
     * Parses Youtube title into list of words and returns index of title/artist separation
     *
     * @param youtubeTitle String of the Youtube title
     * @param yTitleFirst  Will contain the first half or all the string from youtubeTitle, depending on if the title/artist separator can be identified
     * @param yTitleSecond Will contains the remaining strings from youtubeTitle, null otherwise
     * @return if true title/artist can be identified, false otherwise
     */
    public boolean parseYoutubeTitleOld(String youtubeTitle, YoutubeTitleSets yTitle) {
        boolean pastDash = false;
        StringBuilder word = new StringBuilder();
        int i = 0;
        while (i < youtubeTitle.length()) {
            char c = youtubeTitle.charAt(i);
            if (c == '-' || c == '—' || c == '~') {
                pastDash = true;
            } else if (!word.isEmpty() && (Character.isWhitespace(c) || i == youtubeTitle.length() - 1)) {
                String temp = word.toString().replaceAll("\\p{Punct}+$", "");
                if (!pastDash) {
                    yTitle.getyTitleFirstOriginal().add(temp);
                    yTitle.getyTitleFirstUnhomoglyph().add(AnyAscii.transliterate(temp));
                } else {
                    yTitle.getyTitleSecondOriginal().add(temp);
                    yTitle.getyTitleSecondUnhomoglyph().add(AnyAscii.transliterate(temp));
                }

                word = new StringBuilder();
            } else {
                if (!Character.isLetterOrDigit(c)) {
                    i++;
                    continue;
                }

                word.append(Character.toLowerCase(c));
            }

            i++;
        }


        return pastDash;
    }

    /**
     * Parses Youtube title into list of words and returns index of title/artist separation
     *
     * @param youtubeTitle String of the Youtube title
     * @param yTitle Holds an object which contains sets of words pre- and post- dash in both its alphanumeric and unhomoglyphed variations
     * @return if true title/artist separation can be identified, false otherwise
     */
    public boolean parseYoutubeTitle(String youtubeTitle, YoutubeTitleSets yTitle) {
        String[] youtubeTitleSplit = youtubeTitle.split("\\s+");
        int dashCount = 0;
        for (String word : youtubeTitleSplit) {
            if (word.equals("-") || word.equals("—") || word.equals("~")) {
                dashCount++;
                continue;
            }

            word = word.replaceFirst("^[^a-zA-Z]+", "");
            word = word.replaceAll("[^a-zA-Z]+$", "");

            if (dashCount < 1) {
                word = word.toLowerCase();
                yTitle.getyTitleFirstOriginal().add(word);
                yTitle.getyTitleFirstUnhomoglyph().add(AnyAscii.transliterate(word));
            } else {
                word = word.toLowerCase();
                yTitle.getyTitleSecondOriginal().add(word);
                yTitle.getyTitleSecondUnhomoglyph().add(word);
            }
        }

        if (dashCount == 1) {
            return true;
        }

        yTitle.combine();
        return false;
    }

    public double jaccardIndex(Set<String> youtubeSongInfo, Set<String> spotifySongInfo) {
        Set<String> intersection = new HashSet<>(youtubeSongInfo);
        intersection.retainAll(spotifySongInfo);

        Set<String> union = new HashSet<>(youtubeSongInfo);
        union.addAll(spotifySongInfo);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / (double) union.size();
    }

    /**Returns the percentage of which setA is a subset of setB
     * @param setA the possible subset
     * @param setB the set being compared against*/
    public <T> double subSetPercentage(Set<T> setA, Set<T> setB) {
        if (setA.isEmpty()) {
            return 0.0;
        }
        if (setA.size() > setB.size()) {
            return 0;
        }

        Set<T> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);

        return (double) intersection.size() / setA.size();
    }

}
