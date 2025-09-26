package Youtube.Logic;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class PlaylistItems {
    JsonNode root;

    /**PlaylistItems constructor
     * @param node The root node of the PlayListItem JSON*/
    PlaylistItems(JsonNode node) {
        this.root = node;
    }

    /**Returns a list of TitleDescriptions*/
    public List<TitleDescription> getTitleDescriptions() {
        List<TitleDescription> res = new ArrayList<>();
        JsonNode items = root.get("items");
        for (JsonNode node : items) {
            String title = node.get("snippet").get("title").toString();
            title = title.substring(1, title.length() - 1);


            String description = node.get("snippet").get("description").toString();
            description = description.substring(1, description.length() - 1);

            TitleDescription titleDescription = new TitleDescription(title, description);
            res.add(titleDescription);
        }

        return res;
    }
}
