package crawler.api;

import crawler.api.dto.CommentThread;
import crawler.api.dto.YoutubeVideo;
import crawler.api.dto.YoutubeVideoWithComments;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@RestController
@RequestMapping("/api/youtube")
@RequiredArgsConstructor
public class YoutubeController {

    private final YoutubeApiService youTubeApiService;

    @GetMapping("/channel/{channelId}/videos")
    public Flux<YoutubeVideo> getChannelVideos(@PathVariable String channelId) {
        return youTubeApiService.getAllVideosFromChannel(channelId);
    }

    @GetMapping("/channel/{channelId}/videos/list")
    public Mono<List<YoutubeVideo>> getChannelVideosList(@PathVariable String channelId) {
        return youTubeApiService.getAllVideosFromChannel(channelId)
                .collectList();
    }

    @GetMapping("/channel/search")
    public Flux<YoutubeVideo> searchChannelVideos(@RequestParam("channelName") String channelName) {
        return youTubeApiService.getAllVideosFromChannelByName(channelName);
    }

    @GetMapping("/video/{videoId}/comments")
    public Flux<CommentThread> getVideoComments(
            @PathVariable("videoId") String videoId,
            @RequestParam(value = "maxResults", defaultValue = "20") Integer maxResults) {
        return youTubeApiService.getVideoComments(videoId, maxResults);
    }

    @GetMapping("/video/{videoId}/with-comments")
    public Mono<YoutubeVideoWithComments> getVideoWithComments(
            @PathVariable("videoId") String videoId,
            @RequestParam(value = "maxComments", defaultValue = "50") Integer maxComments) {
        return youTubeApiService.getVideoWithComments(videoId, maxComments);
    }

    @GetMapping("/channel/{channelId}/videos-with-comments")
    public Flux<YoutubeVideoWithComments> getChannelVideosWithComments(
            @PathVariable("channelId") String channelId,
            @RequestParam(value = "maxCommentsPerVideo", defaultValue = "10") Integer maxCommentsPerVideo) {
        return youTubeApiService.getAllVideosWithCommentsFromChannel(channelId, maxCommentsPerVideo);
    }

    @GetMapping("/channel/search/{channelName}/videos-with-comments")
    public Flux<YoutubeVideoWithComments> searchChannelVideosWithComments(
            @PathVariable("channelName") String channelName,
            @RequestParam(value = "maxCommentsPerVideo", defaultValue = "10") Integer maxCommentsPerVideo) {
        return youTubeApiService.getAllVideosFromChannelByName(channelName)
                .flatMap(video -> {
                    String videoId = null;
                    if (video.getSnippet().getResourceId() != null) {
                        videoId = video.getSnippet().getResourceId().getVideoId();
                    } else {
                        videoId = video.getVideoId();
                    }

                    if (videoId == null) {
                        return Mono.empty();
                    }

                    return youTubeApiService.getVideoWithComments(videoId, maxCommentsPerVideo);
                }, 2);
    }
}
