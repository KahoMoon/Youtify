package Model;
import com.anyascii.AnyAscii;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.*;
import java.util.stream.Collectors;

public class SongComparisons {

    /**Holds the separated sections of a Youtube title in its original form and an unhomoglyphed form*/
    public class YoutubeTitleSets {
        public boolean identified = false;  //flags if the seperater could be identified or not
        Set<String> youtubeTitleSetPreSeparator;    //holds the pre-seperator set or the entire set if a separator cannot be identified
        Set<String> youtubeTitleSetPreSeparatorUnhomoglyph;    //hold the unhomoglyphed pre-separator set or the entire set if a separator cannot be identified
        StringBuilder youtubeTitlePreSeparator;
        StringBuilder youtubeTitlePreSeparatorUnhomoglyph;

        Set<String> youtubeTitleSetPostSeparator;  //holds the post-seperator set
        Set<String> youtubeTitleSetPostSeparatorUnhomoglyph;   //holds the unhomoglyphed post-separator set
        StringBuilder youtubeTitlePostSeparator;
        StringBuilder youtubeTitlePostSeparatorUnhomoglyph;

        YoutubeTitleSets(String youtubeTitle) {
            this.youtubeTitleSetPreSeparator = new HashSet<>();
            this.youtubeTitleSetPreSeparatorUnhomoglyph = new HashSet<>();
            this.youtubeTitleSetPostSeparator = new HashSet<>();
            this.youtubeTitleSetPostSeparatorUnhomoglyph = new HashSet<>();

            parseYoutubeTitle(youtubeTitle);
        }

        void combine() {
            this.youtubeTitleSetPreSeparator.addAll(this.youtubeTitleSetPostSeparator);
            this.youtubeTitleSetPreSeparatorUnhomoglyph.addAll(this.youtubeTitleSetPostSeparatorUnhomoglyph);
            this.youtubeTitleSetPostSeparator = new HashSet<>();
            this.youtubeTitleSetPostSeparatorUnhomoglyph = new HashSet<>();
        }

        /**
         * Parses Youtube title into list of words and returns index of title/artist separation
         *
         * @param youtubeTitle String of the Youtube title
         * @return if true title/artist separation can be identified, false otherwise
         */
        public boolean parseYoutubeTitle(String youtubeTitle) {
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
                return true;
            }

            this.combine();
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            YoutubeTitleSets that = (YoutubeTitleSets) o;
            return youtubeTitleSetPreSeparator.equals(that.youtubeTitleSetPreSeparator) && youtubeTitleSetPreSeparatorUnhomoglyph.equals(that.youtubeTitleSetPreSeparatorUnhomoglyph) && youtubeTitleSetPostSeparator.equals(that.youtubeTitleSetPostSeparator) && youtubeTitleSetPostSeparatorUnhomoglyph.equals(that.youtubeTitleSetPostSeparatorUnhomoglyph);
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
    public boolean compareSong(JsonNode youtubeSongJSON, JsonNode spotifySongJSON) {
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

    private double checkTitle(String youtubeTitle, String spotifyTitle) {
        double res = 0.0;

        Set<String> spotifyTitleSet = parseSpotifyTitle(spotifyTitle);
        YoutubeTitleSets youtubeTitleSets = new YoutubeTitleSets(youtubeTitle);

        res = Math.max(res, subSetPercentage(spotifyTitleSet, youtubeTitleSets.youtubeTitleSetPostSeparator));
        res = Math.max(res, subSetPercentage(spotifyTitleSet, youtubeTitleSets.youtubeTitleSetPostSeparatorUnhomoglyph));
        res = Math.max(res, jaccardIndex(spotifyTitleSet, youtubeTitleSets.youtubeTitleSetPostSeparator));
        res = Math.max(res, jaccardIndex(spotifyTitleSet, youtubeTitleSets.youtubeTitleSetPostSeparatorUnhomoglyph));

        if (res < CONFIDENCEINTERVAL) {
            res = Math.max(res, subSetPercentage(spotifyTitleSet, youtubeTitleSets.youtubeTitleSetPreSeparator));
            res = Math.max(res, subSetPercentage(spotifyTitleSet, youtubeTitleSets.youtubeTitleSetPreSeparatorUnhomoglyph));
            res = Math.max(res, jaccardIndex(spotifyTitleSet, youtubeTitleSets.youtubeTitleSetPreSeparator));
            res = Math.max(res, jaccardIndex(spotifyTitleSet, youtubeTitleSets.youtubeTitleSetPreSeparatorUnhomoglyph));
        }

        return res;
    }

    /**Returns the probability that the Youtube */
    public double checkArtist(String youtubeChannel, String spotifyArtist, final YoutubeTitleSets youtubeTitleSets) {
        double res = 0.0;

        if (youtubeTitleSets.identified) {
            String youtubeTitlePreSeparatorToString = youtubeTitleSets.youtubeTitlePreSeparator.toString();
            res = Math.max(res, jaro_distance(spotifyArtist, youtubeTitlePreSeparatorToString));

            String youtubeTitlePreSeparatorUnhomoglyphToString = youtubeTitleSets.youtubeTitlePreSeparatorUnhomoglyph.toString();
            if (res < CONFIDENCEINTERVAL) {
                res = Math.max(res, jaro_distance(spotifyArtist, youtubeTitlePreSeparatorUnhomoglyphToString));
            }

            if (res < CONFIDENCEINTERVAL) {
                res = Math.max(res, levenshteinDistance(spotifyArtist, youtubeTitlePreSeparatorToString));
            }

            if (res < CONFIDENCEINTERVAL) {
                res = Math.max(res, levenshteinDistance(spotifyArtist, youtubeTitlePreSeparatorUnhomoglyphToString));
            }
        }

        if (!youtubeTitleSets.identified || res < CONFIDENCEINTERVAL) {
            Set<String> youtubeSet = Arrays.stream(youtubeChannel.split("\\s+")).collect(Collectors.toSet());
            Set<String> spotifySet = Arrays.stream(spotifyArtist.split("\\s+")).collect(Collectors.toSet());

            res = Math.max(res, subSetPercentage(spotifySet, youtubeTitleSets.youtubeTitleSetPreSeparator));
            res = Math.max(res, subSetPercentage(spotifySet, youtubeTitleSets.youtubeTitleSetPreSeparatorUnhomoglyph));
            res = Math.max(res, subSetPercentage(spotifySet, youtubeTitleSets.youtubeTitleSetPostSeparator));
            res = Math.max(res, subSetPercentage(spotifySet, youtubeTitleSets.youtubeTitleSetPostSeparatorUnhomoglyph));


            if (res < CONFIDENCEINTERVAL) {
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

    public double checkLength(int youtubeRuntime, int spotifyRuntime) {
        int longer = Math.max(youtubeRuntime, spotifyRuntime);
        int shorter = Math.min(youtubeRuntime, spotifyRuntime);

        return (double) shorter / longer;
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

    /**Returns the jaccard index of the given sets
     * @param youtubeSongSet a set of strings that refer to the Youtube video
     * @param spotifySongSet a set of strings that refer to the Spotify song
     * @return the jaccard index*/
    public double jaccardIndex(Set<String> youtubeSongSet, Set<String> spotifySongSet) {
        Set<String> intersection = new HashSet<>(youtubeSongSet);
        intersection.retainAll(spotifySongSet);

        Set<String> union = new HashSet<>(youtubeSongSet);
        union.addAll(spotifySongSet);

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
