package Youtube.Data;

public enum YoutubeApi {
    API_KEY;

    private static String youtubeDataApiKey = "AIzaSyD-UKTaIEV2m_CCJtkx6mLLtq4dqWwQoWw";

    public static String getId() {
        return youtubeDataApiKey;
    }
}
