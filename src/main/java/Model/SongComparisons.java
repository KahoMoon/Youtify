package Model;
import com.anyascii.AnyAscii;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class SongComparisons {

    /**Holds the separated sections of a YouTube title in its original form and an unhomoglyphed form*/
    public class YoutubeTitleSets {
        public boolean identified = false;  //flags if the separate could be identified or not
        Set<String> youtubeTitleSetPreSeparator;    //holds the pre-separator set or the entire set if a separator cannot be identified
        Set<String> youtubeTitleSetPreSeparatorUnhomoglyph;    //holds the unhomoglyphed pre-separator set or the entire set if a separator cannot be identified
        StringBuilder youtubeTitlePreSeparator; //holds the pre-separator title
        StringBuilder youtubeTitlePreSeparatorUnhomoglyph;  //holds the unhomoglyphed pre-separator title

        Set<String> youtubeTitleSetPostSeparator;  //holds the post-separator set
        Set<String> youtubeTitleSetPostSeparatorUnhomoglyph;   //holds the unhomoglyphed post-separator set
        StringBuilder youtubeTitlePostSeparator;    //holds the post-separator title
        StringBuilder youtubeTitlePostSeparatorUnhomoglyph; //holds the unhomoglyphed post-separator title

        YoutubeTitleSets() {
            this.youtubeTitleSetPreSeparator = new HashSet<>();
            this.youtubeTitleSetPreSeparatorUnhomoglyph = new HashSet<>();
            this.youtubeTitleSetPostSeparator = new HashSet<>();
            this.youtubeTitleSetPostSeparatorUnhomoglyph = new HashSet<>();
        }

        YoutubeTitleSets(String youtubeTitle) {
            this.youtubeTitleSetPreSeparator = new HashSet<>();
            this.youtubeTitleSetPreSeparatorUnhomoglyph = new HashSet<>();
            this.youtubeTitleSetPostSeparator = new HashSet<>();
            this.youtubeTitleSetPostSeparatorUnhomoglyph = new HashSet<>();

            parseYoutubeTitle(youtubeTitle);
        }

        /**
         * Merges post-separator objects with their pre-separator counterparts and removes them
         */
        void combine() {
            this.youtubeTitleSetPreSeparator.addAll(this.youtubeTitleSetPostSeparator);
            this.youtubeTitleSetPreSeparatorUnhomoglyph.addAll(this.youtubeTitleSetPostSeparatorUnhomoglyph);
            this.youtubeTitleSetPostSeparator = new HashSet<>();
            this.youtubeTitleSetPostSeparatorUnhomoglyph = new HashSet<>();

            this.youtubeTitlePreSeparator.append(" ").append(youtubeTitlePostSeparator);
            this.youtubeTitlePreSeparatorUnhomoglyph.append(" ").append(youtubeTitlePostSeparatorUnhomoglyph);
            this.youtubeTitlePostSeparator = null;
            this.youtubeTitlePostSeparatorUnhomoglyph = null;
        }

        /**
         * Parses YouTube title into a StringBuilder and set of its pre- and post-words in both its homoglyphed and unhomoglyphed forms
         * @param youtubeTitle String of the YouTube title
         */
        private void parseYoutubeTitle(String youtubeTitle) {
            String[] youtubeTitleSplit = youtubeTitle.split("\\s+");
            int dashCount = 0;
            for (String word : youtubeTitleSplit) {
                if (word.equals("-") || word.equals("—") || word.equals("~")) {
                    dashCount++;
                    continue;
                }

                word = word.replaceFirst("^[^a-zA-Z]+", "");    //removes non-alphabetic character at start of word
                word = word.replaceAll("[^a-zA-Z]+$", "");  //removes non-alphabetic characters at end of word

                if (word.isBlank()) {
                    continue;
                }

                if (dashCount < 1) {
                    word = word.toLowerCase();
                    this.youtubeTitleSetPreSeparator.add(word);
                    assert this.youtubeTitlePreSeparator != null;
                    this.youtubeTitlePreSeparator.append(word);
                    this.youtubeTitleSetPreSeparatorUnhomoglyph.add(AnyAscii.transliterate(word));
                    assert this.youtubeTitlePreSeparatorUnhomoglyph != null;
                    this.youtubeTitlePreSeparatorUnhomoglyph.append(word);
                } else {
                    word = word.toLowerCase();
                    this.youtubeTitleSetPostSeparator.add(word);
                    this.youtubeTitlePostSeparator.append(word);
                    this.youtubeTitleSetPostSeparatorUnhomoglyph.add(word);
                    assert this.youtubeTitlePostSeparatorUnhomoglyph != null;
                    this.youtubeTitlePostSeparatorUnhomoglyph.append(word);
                }
            }

            if (dashCount == 1) {
                identified = true;
                return;
            }

            this.combine();
        }

        /**
         * Checks the title for any red flag words
         * @return returns true if the title contains any red flag words
         */
        public boolean hasRedFlag() {
            for (String word : RED_FLAG_WORDS) {
                if (youtubeTitleSetPreSeparator.contains(word) || youtubeTitleSetPreSeparatorUnhomoglyph.contains(word) || youtubeTitleSetPostSeparator.contains(word) || youtubeTitleSetPostSeparatorUnhomoglyph.contains(word)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            YoutubeTitleSets that = (YoutubeTitleSets) o;
            return youtubeTitleSetPreSeparator.equals(that.youtubeTitleSetPreSeparator) && youtubeTitleSetPreSeparatorUnhomoglyph.equals(that.youtubeTitleSetPreSeparatorUnhomoglyph) && youtubeTitleSetPostSeparator.equals(that.youtubeTitleSetPostSeparator) && youtubeTitleSetPostSeparatorUnhomoglyph.equals(that.youtubeTitleSetPostSeparatorUnhomoglyph);
        }
    }

    File RED_FLAG;
    File GREEN_FLAG;
    final double CONFIDENCE_INTERVAL = 0.8;
    final Set<String> RED_FLAG_WORDS = new HashSet<>(Arrays.asList("cover", "remix", "flip", "instrumental", "live", "acoustic", "ver", "version", "mashup", "edit", "slowed", "doomer", "nightcore"));
    final int RED_FLAG_LENGTH = 300;
    final double MATCH_PROBABILITY = 1.0;
    int CHECK_ARTIST_LIMIT = 2;   //the max number of artists that will be compared between the YouTube and Spotify tracks

    public SongComparisons() {
        RED_FLAG = new File("Youtify RedFlags" + Instant.now() + ".txt");
        GREEN_FLAG = new File("Youtify GreenFlags" + Instant.now() + ".txt");
    }

    /**
     * Compares the similarity between a YouTube video and Spotify song
     * @param youtubeSongTitle title of the YouTube video
     * @param spotifySongTitle title of the track on Spotify
     * @param youtubeSongLen length of the YouTube video in seconds
     * @param spotifySongLen length of the Spotify song in seconds
     * @return the probability the YouTube video and Spotify song refer to the same entity as a number between 0 and 1
     */
    public double compareSong(String youtubeSongTitle, String spotifySongTitle, String youtubeChannel, String spotifyArtist, int youtubeSongLen, int spotifySongLen) throws IOException {
        YoutubeTitleSets youtubeTitleSets = new YoutubeTitleSets();
        double titleSimilarity = checkTitle(youtubeSongTitle, spotifySongTitle, youtubeTitleSets);
        double artistSimilarity = checkArtist(youtubeChannel, spotifyArtist, youtubeTitleSets);
        double lengthSimilarity = checkLength(youtubeSongLen, spotifySongLen);

        double res = titleSimilarity * artistSimilarity * lengthSimilarity;

        FileWriter myWriter;
        if (res >= CONFIDENCE_INTERVAL) {
            myWriter = new FileWriter(GREEN_FLAG, true);
            myWriter.write(youtubeSongTitle + "\n" +
                    youtubeChannel + "\n" +
                    youtubeSongLen + "\n");
        } else {
            myWriter = new FileWriter(RED_FLAG, true);
            myWriter.write(youtubeSongTitle + "\n" +
                    youtubeChannel + "\n" +
                    youtubeSongLen + "\n");
        }
        myWriter.close();

        return res;
    }

    /**
     * Parses and compares the YouTube title against the Spotify title
     * @param youtubeTitle title of the YouTube video
     * @param spotifyTitle title of the Spotify track
     * @param youtubeTitleSets object holding the parsed YouTube title
     * @return the probability the YouTube title and Spotify title refer to the same entity as a number between 0 and 1
     */
    private double checkTitle(String youtubeTitle, String spotifyTitle, YoutubeTitleSets youtubeTitleSets) {
        double res = 0.0;

        Set<String> spotifyTitleSet = parseSpotifyTitle(spotifyTitle);
        youtubeTitleSets = new YoutubeTitleSets(youtubeTitle);

        if (youtubeTitleSets.hasRedFlag()) {
            return 0.0;
        }

        res = Math.max(res, subSetPercentage(spotifyTitleSet, youtubeTitleSets.youtubeTitleSetPostSeparator));
        res = Math.max(res, subSetPercentage(spotifyTitleSet, youtubeTitleSets.youtubeTitleSetPostSeparatorUnhomoglyph));
        res = Math.max(res, jaccardIndex(spotifyTitleSet, youtubeTitleSets.youtubeTitleSetPostSeparator));
        res = Math.max(res, jaccardIndex(spotifyTitleSet, youtubeTitleSets.youtubeTitleSetPostSeparatorUnhomoglyph));

        if (res < CONFIDENCE_INTERVAL) {
            res = Math.max(res, subSetPercentage(spotifyTitleSet, youtubeTitleSets.youtubeTitleSetPreSeparator));
            res = Math.max(res, subSetPercentage(spotifyTitleSet, youtubeTitleSets.youtubeTitleSetPreSeparatorUnhomoglyph));
            res = Math.max(res, jaccardIndex(spotifyTitleSet, youtubeTitleSets.youtubeTitleSetPreSeparator));
            res = Math.max(res, jaccardIndex(spotifyTitleSet, youtubeTitleSets.youtubeTitleSetPreSeparatorUnhomoglyph));
        }

        return res;
    }

    /**
     * Compares the YouTube channel name against the Spotify artist name
     * @param youtubeChannel name of the YouTube channel
     * @param spotifyArtist name of the Spotify artist
     * @param youtubeTitleSets object holding a parsed YouTube title
     * @return the probability the YouTube channel refers to the Spotify artist as a number between 0 and 1
     */
    private double checkArtist(String youtubeChannel, String spotifyArtist, YoutubeTitleSets youtubeTitleSets) {
        double res = 0.0;

        if (youtubeTitleSets.identified) {
            String youtubeTitlePreSeparatorToString = youtubeTitleSets.youtubeTitlePreSeparator.toString();
            res = Math.max(res, jaro_distance(spotifyArtist, youtubeTitlePreSeparatorToString));

            String youtubeTitlePreSeparatorUnhomoglyphToString = youtubeTitleSets.youtubeTitlePreSeparatorUnhomoglyph.toString();
            if (res < CONFIDENCE_INTERVAL) {
                res = Math.max(res, jaro_distance(spotifyArtist, youtubeTitlePreSeparatorUnhomoglyphToString));
            }

            if (res < CONFIDENCE_INTERVAL) {
                res = Math.max(res, levenshteinDistance(spotifyArtist, youtubeTitlePreSeparatorToString));
            }

            if (res < CONFIDENCE_INTERVAL) {
                res = Math.max(res, levenshteinDistance(spotifyArtist, youtubeTitlePreSeparatorUnhomoglyphToString));
            }
        }

        if (!youtubeTitleSets.identified || res < CONFIDENCE_INTERVAL) {
            Set<String> youtubeSet = Arrays.stream(youtubeChannel.split("\\s+")).collect(Collectors.toSet());
            Set<String> spotifySet = Arrays.stream(spotifyArtist.split("\\s+")).collect(Collectors.toSet());

            res = Math.max(res, subSetPercentage(spotifySet, youtubeTitleSets.youtubeTitleSetPreSeparator));
            res = Math.max(res, subSetPercentage(spotifySet, youtubeTitleSets.youtubeTitleSetPreSeparatorUnhomoglyph));
            res = Math.max(res, subSetPercentage(spotifySet, youtubeTitleSets.youtubeTitleSetPostSeparator));
            res = Math.max(res, subSetPercentage(spotifySet, youtubeTitleSets.youtubeTitleSetPostSeparatorUnhomoglyph));


            if (res < CONFIDENCE_INTERVAL) {
                res = Math.max(res, jaccardIndex(spotifySet, youtubeTitleSets.youtubeTitleSetPreSeparator));
                res = Math.max(res, jaccardIndex(spotifySet, youtubeTitleSets.youtubeTitleSetPreSeparatorUnhomoglyph));
                res = Math.max(res, jaccardIndex(spotifySet, youtubeTitleSets.youtubeTitleSetPostSeparator));
                res = Math.max(res, jaccardIndex(spotifySet, youtubeTitleSets.youtubeTitleSetPostSeparatorUnhomoglyph));
                res = Math.max(res, jaccardIndex(spotifySet, youtubeSet));
                res = Math.max(res, subSetPercentage(spotifySet, youtubeSet));
            }
        }

        return res;
    }

    /**
     * Compares the YouTube and Spotify runtimes
     * @param youtubeRuntime YouTube video runtime in seconds
     * @param spotifyRuntime Spotify track runtime in seconds
     * @return the similarity between the runtimes as number between 0 and 1
     */
    private double checkLength(int youtubeRuntime, int spotifyRuntime) {
        int longer = Math.max(youtubeRuntime, spotifyRuntime);
        int shorter = Math.min(youtubeRuntime, spotifyRuntime);

        return (double) shorter / longer;
    }

    /**
     * Parses a title from Spotify
     * @param spotifyTitle title of Spotify track
     * @return set containing all parsed words
     */
    private Set<String> parseSpotifyTitle(String spotifyTitle) {
        Set<String> spotifyTitleSet = new HashSet<>();
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

        return spotifyTitleSet;
    }

    /**Returns the Jaccard index of the given sets*/
    private static double jaccardIndex(Set<String> setA, Set<String> setB) {
        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);

        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / (double) union.size();
    }

    /**Returns the percentage of which setA is a subset of setB as a metric between 0.0 (no intersection at all) and 1.0 (setA and setB are equal).
     * @param setA the possible subset
     * @param setB the set being compared against*/
    private static <T> double subSetPercentage(Set<T> setA, Set<T> setB) {
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

    /**
     * Returns the Jaro-Winkler Similarity between the two strings as a score between 0.0 and 1.0
     */
    //from GeeksForGeeks
    private static double jaro_distance(String s1, String s2) {
        // If the Strings are equal
        if (Objects.equals(s1, s2))
            return 1.0;

        // Length of two Strings
        int len1 = s1.length(),
                len2 = s2.length();

        // Maximum distance upto which matching
        // is allowed
        int max_dist = (int) (Math.floor(Math.max(len1, len2) / 2) - 1);

        // Count of matches
        int match = 0;

        // Hash for matches
        int[] hash_s1 = new int[s1.length()];
        int[] hash_s2 = new int[s2.length()];

        // Traverse through the first String
        for (int i = 0; i < len1; i++)
        {

            // Check if there is any matches
            for (int j = Math.max(0, i - max_dist);
                 j < Math.min(len2, i + max_dist + 1); j++)

                // If there is a match
                if (s1.charAt(i) == s2.charAt(j) && hash_s2[j] == 0)
                {
                    hash_s1[i] = 1;
                    hash_s2[j] = 1;
                    match++;
                    break;
                }
        }

        // If there is no match
        if (match == 0)
            return 0.0;

        // Number of transpositions
        double t = 0;

        int point = 0;

        // Count number of occurrences
        // where two characters match but
        // there is a third matched character
        // in between the indices
        for (int i = 0; i < len1; i++)
            if (hash_s1[i] == 1)
            {

                // Find the next matched character
                // in second String
                while (hash_s2[point] == 0)
                    point++;

                if (s1.charAt(i) != s2.charAt(point++) )
                    t++;
            }

        t /= 2;

        // Return the Jaro Similarity
        return (((double)match) / ((double)len1)
                + ((double)match) / ((double)len2)
                + ((double)match - t) / ((double)match))
                / 3.0;
    }

    /**Returns the Levenshtein Distance between the two string argument as a metric between 0.0 and 1.0
    */
    //from GeeksForGeeks
    private static int levenshteinDistance(String str1,
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
    private static int NumOfReplacement(char c1, char c2) {
        return c1 == c2 ? 0 : 1;
    }

    // receives the count of different
    // operations performed and returns the
    // minimum value among them.
    //from GeeksForGeeks
    private static int minm_edits(int... nums) {
        return Arrays.stream(nums).min().orElse(
                Integer.MAX_VALUE);
    }
}
