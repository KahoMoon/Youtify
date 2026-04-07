package Model;
import com.anyascii.AnyAscii;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.*;

public class SongComparisons {

    /**Holds the separated sections of a Youtube title in its original form and an unhomoglyphed form*/
    public static class YoutubeTitleSets {
        public boolean identified = false;
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

    public SongComparisons() {
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

        YoutubeTitleSets youtubeTitleSets =  new YoutubeTitleSets();
        res *= checkTitle(youtubeSongJSON, spotifySongJSON, youtubeTitleSets);
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

    /**Returns the probability that the Youtube title and Spotify title refer to the same object
     * @return Returns probability that the Youtube JSON and Spotify JSON contain identical titles. If perfect match, returns 1.
     * @param youtubeSongJSON "items" sub-JSON from Youtube API
     * @param spotifySongJSON "items" sub-JSON from Spotify Data API*/
    public double checkTitle(JsonNode youtubeSongJSON, JsonNode spotifySongJSON, YoutubeTitleSets youtubeTitleSets) {
        double res = 0.0;

        String spotifyTitle = spotifySongJSON.get("name").asText();
        String youtubeTitle = youtubeSongJSON.get("title").asText();
        Set<String> spotifyTitleSet = new HashSet<>();
        parseYoutubeTitle(youtubeTitle, youtubeTitleSets);
        parseSpotifyTitle(spotifyTitle, spotifyTitleSet);

        res = Math.max(res, subSetPercentage(spotifyTitleSet, youtubeTitleSets.getyTitleSecondOriginal()));
        if (res == 1) {
            return res;
        }
        res = Math.max(res, subSetPercentage(spotifyTitleSet, youtubeTitleSets.getyTitleSecondUnhomoglyph()));
        if (res == 1) {
            return res;
        }
        res = Math.max(res, subSetPercentage(spotifyTitleSet, youtubeTitleSets.getyTitleFirstOriginal()));
        if (res == 1) {
            return res;
        }
        res = Math.max(res, subSetPercentage(spotifyTitleSet, youtubeTitleSets.getyTitleFirstUnhomoglyph()));

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

    //from GeeksForGeeks
    static int compute_Levenshtein_distanceDP(String str1,
                                              String str2)
    {

        // A 2-D matrix to store previously calculated
        // answers of subproblems in order
        // to obtain the final

        int[][] dp = new int[str1.length() + 1][str2.length() + 1];

        for (int i = 0; i <= str1.length(); i++)
        {
            for (int j = 0; j <= str2.length(); j++) {

                // If str1 is empty, all characters of
                // str2 are inserted into str1, which is of
                // the only possible method of conversion
                // with minimum operations.
                if (i == 0) {
                    dp[i][j] = j;
                }

                // If str2 is empty, all characters of str1
                // are removed, which is the only possible
                //  method of conversion with minimum
                //  operations.
                else if (j == 0) {
                    dp[i][j] = i;
                }

                else {
                    // find the minimum among three
                    // operations below


                    dp[i][j] = minm_edits(dp[i - 1][j - 1]
                                    + NumOfReplacement(str1.charAt(i - 1),str2.charAt(j - 1)), // replace
                            dp[i - 1][j] + 1, // delete
                            dp[i][j - 1] + 1); // insert
                }
            }
        }

        return dp[str1.length()][str2.length()];
    }

    // check for distinct characters
    // in str1 and str2
    //from GeeksForGeeks
    static int NumOfReplacement(char c1, char c2)
    {
        return c1 == c2 ? 0 : 1;
    }

    // receives the count of different
    // operations performed and returns the
    // minimum value among them.
    //from GeeksForGeeks
    static int minm_edits(int... nums)
    {
        return Arrays.stream(nums).min().orElse(
                Integer.MAX_VALUE);
    }
}
