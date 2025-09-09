package crawler.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class YoutubeVideo {
    private String kind;
    private String etag;
    private Object id; // Search API와 Playlist API에서 다른 형태로 옴
    private Snippet snippet;

    // Search API용 id 가져오기
    public String getVideoId() {
        if (id instanceof String) {
            return (String) id;
        } else if (id instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> idMap = (Map<String, Object>) id;
            return (String) idMap.get("videoId");
        }
        return null;
    }

    // Channel ID 가져오기 (Search API용)
    public String getChannelIdFromId() {
        if (id instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> idMap = (Map<String, Object>) id;
            return (String) idMap.get("channelId");
        }
        return null;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Snippet {
        private String publishedAt;
        private String channelId;
        private String title;
        private String description;
        private Thumbnails thumbnails;
        private String channelTitle;
        private String playlistId;
        private Integer position;
        private ResourceId resourceId;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Thumbnails {
            @JsonProperty("default")
            private Thumbnail defaultThumbnail;
            private Thumbnail medium;
            private Thumbnail high;
            private Thumbnail standard;
            private Thumbnail maxres;

            @Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Thumbnail {
                private String url;
                private int width;
                private int height;
            }
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ResourceId {
            private String kind;
            private String videoId;
        }
    }
}