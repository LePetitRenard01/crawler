package crawler.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.*;
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class YoutubeChannelResponse {
    private String kind;
    private String etag;
    private PageInfo pageInfo;
    private List<YouTubeChannel> items;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class YouTubeChannel {
        private String kind;
        private String etag;
        private String id;
        private ContentDetails contentDetails;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ContentDetails {
            private RelatedPlaylists relatedPlaylists;

            @Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class RelatedPlaylists {
                private String likes;
                private String uploads;
            }
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageInfo {
        private int totalResults;
        private int resultsPerPage;
    }
}
