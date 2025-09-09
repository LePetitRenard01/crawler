package crawler.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class YoutubeCommentResponse {
    private String kind;
    private String etag;
    private String nextPageToken;
    private PageInfo pageInfo;
    private List<CommentThread> items;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageInfo {
        private Integer totalResults;
        private Integer resultsPerPage;
    }
}
