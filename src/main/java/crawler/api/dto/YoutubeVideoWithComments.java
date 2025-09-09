package crawler.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class YoutubeVideoWithComments {
    private String videoId;
    private String title;
    private String description;
    private String publishedAt;
    private String channelTitle;
    private String channelId;
    private String thumbnailUrl;
    private List<CommentInfo> comments;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommentInfo {
        private String commentId;
        private String authorName;
        private String authorProfileImage;
        private String commentText;
        private Integer likeCount;
        private String publishedAt;
        private String updatedAt;
        private Integer replyCount;
    }

    // YouTubeVideo에서 변환하는 생성자
    public static YoutubeVideoWithComments fromYouTubeVideo(YoutubeVideo video) {
        YoutubeVideoWithComments result = new YoutubeVideoWithComments();

        // 비디오 ID 추출
        String videoId = null;
        if (video.getSnippet().getResourceId() != null) {
            videoId = video.getSnippet().getResourceId().getVideoId();
        } else {
            videoId = video.getVideoId();
        }

        result.setVideoId(videoId);
        result.setTitle(video.getSnippet().getTitle());
        result.setDescription(video.getSnippet().getDescription());
        result.setPublishedAt(video.getSnippet().getPublishedAt());
        result.setChannelTitle(video.getSnippet().getChannelTitle());
        result.setChannelId(video.getSnippet().getChannelId());

        // 썸네일 URL 추출
        if (video.getSnippet().getThumbnails() != null) {
            if (video.getSnippet().getThumbnails().getHigh() != null) {
                result.setThumbnailUrl(video.getSnippet().getThumbnails().getHigh().getUrl());
            } else if (video.getSnippet().getThumbnails().getMedium() != null) {
                result.setThumbnailUrl(video.getSnippet().getThumbnails().getMedium().getUrl());
            }
        }

        result.setComments(new ArrayList<>());
        return result;
    }
}