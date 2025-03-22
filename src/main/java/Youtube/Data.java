package Youtube;

public class Data {

    /**The Youtube Data API endpoint*/
    static final String youtubeApiEndpoint = "https://www.googleapis.com/youtube/v3/";

    /**Returns a query that can be used to access the playlist items fitting the specified parameters. A part and id/playlistId must be given.
     * @param part Cannot be null. The part parameter specifies a comma-separated list of one or more playlistItem resource properties that the API response will include. If the parameter identifies a property that contains child properties, the child properties will be included in the response. The parameter value can be either: contentDetails, id, snippet, or status.
     * @param id May be null if playlistId is not. The id parameter specifies a comma-separated list of one or more unique playlist item IDs.
     * @param playlistId May be null if id is not. The playlistId parameter specifies the unique ID of the playlist for which you want to retrieve playlist items. Note that even though this is an optional parameter, every request to retrieve playlist items must specify a value for either the id parameter or the playlistId parameter.
     * @param maxResults The maxResults parameter specifies the maximum number of items that should be returned in the result set. Acceptable values are 0 to 50, inclusive. The default value is 5.
     * @param onBehalfOfContentOwner This parameter can only be used in a properly authorized request. Note: This parameter is intended exclusively for YouTube content partners.The onBehalfOfContentOwner parameter indicates that the request's authorization credentials identify a YouTube CMS user who is acting on behalf of the content owner specified in the parameter value. This parameter is intended for YouTube content partners that own and manage many different YouTube channels. It allows content owners to authenticate once and get access to all their video and channel data, without having to provide authentication credentials for each individual channel. The CMS account that the user authenticates with must be linked to the specified YouTube content owner.
     * @param pageToken The pageToken parameter identifies a specific page in the result set that should be returned. In an API response, the nextPageToken and prevPageToken properties identify other pages that could be retrieved.
     * @param videoId The videoId parameter specifies that the request should return only the playlist items that contain the specified video.
     * */
    public static String getPlaylistItemQuery(Part part, String id, String playlistId, Integer maxResults, String onBehalfOfContentOwner, String pageToken, String videoId) {
        if (part == null || (id == null && playlistId == null)) {
            throw new IllegalArgumentException();
        }

        StringBuilder res = new StringBuilder();
        res.append(youtubeApiEndpoint);
        res.append("playlistItems?");
        res.append("key=").append(YoutubeApi.getId()).append("&");
        res.append("part=").append(part).append("&");

        if (id == null) {
            res.append("playlistId=").append(playlistId).append("&");
        } else {
            res.append("id=").append(id).append("&");
        }

        if (playlistId != null) {
            res.append("playlistId=").append(playlistId).append("&");
        }
        if (maxResults != null) {
            res.append("maxResults=").append(maxResults).append("&");
        }
        if (onBehalfOfContentOwner != null) {
            res.append("onBehalfOfContentOwner=").append(onBehalfOfContentOwner).append("&");
        }
        if (pageToken != null) {
            res.append("pageToken=").append(pageToken).append("&");
        }
        if (videoId != null) {
            res.append("videoId=").append(videoId);
        }

        if (res.toString().charAt(res.length() - 1) == '&') {
            res.deleteCharAt(res.length() - 1);
        }

        return res.toString();
    }
}
