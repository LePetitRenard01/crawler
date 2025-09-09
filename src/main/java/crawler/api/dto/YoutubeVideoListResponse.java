package crawler.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class YoutubeVideoListResponse {
    private String kind;
    private String etag;
    private String nextPageToken;
    private String prevPageToken;
    private PageInfo pageInfo;
    private List<YoutubeVideo> items;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageInfo {
        private int totalResults;
        private int resultsPerPage;
    }
}
