package crawler.api;

import crawler.api.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class YoutubeApiService {

    private final YoutubeApiConfig config;
    private final WebClient webClient;

    /**
     * 채널 ID로 업로드 플레이리스트 ID를 가져옴
     */
    public Mono<String> getUploadPlaylistId(String channelId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/channels")
                        .queryParam("part", "contentDetails")
                        .queryParam("id", channelId)
                        .queryParam("key", config.getKey())
                        .build())
                .retrieve()
                .bodyToMono(YoutubeChannelResponse.class)
                .map(response -> {
                    if (response.getItems() != null && !response.getItems().isEmpty()) {
                        return response.getItems().get(0)
                                .getContentDetails()
                                .getRelatedPlaylists()
                                .getUploads();
                    }
                    throw new RuntimeException("채널을 찾을 수 없습니다: " + channelId);
                })
                .doOnNext(playlistId -> log.info("업로드 플레이리스트 ID: {}", playlistId));
    }

    /**
     * 채널명으로 채널 ID를 검색
     */

    public Mono<String> getChannelIdByName(String channelName) {
        log.info("API 키 확인: {}", config.getKey() != null ? "설정됨" : "설정되지 않음");
        log.info("채널명 검색: {}", channelName);

        return webClient.get()
                .uri(uriBuilder -> {
                    var uri = uriBuilder
                            .path("/search")
                            .queryParam("part", "snippet")
                            .queryParam("type", "channel")
                            .queryParam("q", channelName)
                            .queryParam("key", config.getKey())
                            .queryParam("maxResults", 1)
                            .build();
                    log.info("요청 URL: {}", uri.toString());
                    return uri;
                })
                .retrieve()
                .bodyToMono(YoutubeVideoListResponse.class)
                .map(response -> {
                    if (response.getItems() != null && !response.getItems().isEmpty()) {
                        YoutubeVideo firstResult = response.getItems().get(0);
                        // Search API에서는 snippet.channelId를 사용하거나 id에서 channelId를 추출
                        String channelId = firstResult.getChannelIdFromId();
                        if (channelId == null) {
                            channelId = firstResult.getSnippet().getChannelId();
                        }
                        log.info("찾은 채널 ID: {}", channelId);
                        return channelId;
                    }
                    throw new RuntimeException("채널을 찾을 수 없습니다: " + channelName);
                });
    }

    /**
     * 플레이리스트의 모든 비디오를 페이지별로 가져옴
     */
    public Flux<YoutubeVideo> getAllVideosFromPlaylist(String playlistId) {
        return getVideosFromPlaylist(playlistId, null)
                .expand(response -> {
                    if (response.getNextPageToken() != null) {
                        return getVideosFromPlaylist(playlistId, response.getNextPageToken());
                    } else {
                        return Mono.empty();
                    }
                })
                .flatMapIterable(YoutubeVideoListResponse::getItems);
    }

    /**
     * 특정 페이지의 비디오들을 가져옴
     */
    private Mono<YoutubeVideoListResponse> getVideosFromPlaylist(String playlistId, String pageToken) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path("/playlistItems")
                            .queryParam("part", "snippet")
                            .queryParam("playlistId", playlistId)
                            .queryParam("key", config.getKey())
                            .queryParam("maxResults", 50); // 최대 50개씩

                    if (pageToken != null) {
                        builder.queryParam("pageToken", pageToken);
                    }
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(YoutubeVideoListResponse.class)
                .doOnNext(response ->
                        log.info("페이지 토큰: {}, 비디오 개수: {}",
                                pageToken, response.getItems().size()));
    }

    /**
     * 특정 채널의 모든 비디오를 가져오는 메인 메서드
     */
    public Flux<YoutubeVideo> getAllVideosFromChannel(String channelId) {
        return getUploadPlaylistId(channelId)
                .flatMapMany(this::getAllVideosFromPlaylist);
    }

    /**
     * 채널명으로 모든 비디오를 가져옴
     */
    public Flux<YoutubeVideo> getAllVideosFromChannelByName(String channelName) {
        return getChannelIdByName(channelName)
                .flatMapMany(this::getAllVideosFromChannel);
    }

    /**
     * 특정 비디오의 댓글을 가져옴
     */
    public Flux<CommentThread> getVideoComments(String videoId, Integer maxResults) {
        if (maxResults == null) {
            maxResults = 20; // 기본값
        }

        Integer finalMaxResults = maxResults;
        return getCommentsFromVideo(videoId, null, maxResults)
                .expand(response -> {
                    if (response.getNextPageToken() != null) {
                        return getCommentsFromVideo(videoId, response.getNextPageToken(), finalMaxResults);
                    } else {
                        return Mono.empty();
                    }
                })
                .flatMapIterable(YoutubeCommentResponse::getItems);
    }

    /**
     * 특정 페이지의 댓글들을 가져옴
     */
    private Mono<YoutubeCommentResponse> getCommentsFromVideo(String videoId, String pageToken, Integer maxResults) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path("/commentThreads")
                            .queryParam("part", "snippet")
                            .queryParam("videoId", videoId)
                            .queryParam("key", config.getKey())
                            .queryParam("maxResults", Math.min(maxResults, 100)) // 최대 100개
                            .queryParam("order", "relevance"); // 관련성순 정렬

                    if (pageToken != null) {
                        builder.queryParam("pageToken", pageToken);
                    }
                    return builder.build();
                })
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(),
                        response -> {
                            if (response.statusCode().value() == 403) {
                                log.warn("댓글이 비활성화된 비디오: {}", videoId);
                                return Mono.error(new RuntimeException("댓글이 비활성화된 비디오입니다: " + videoId));
                            }
                            return response.createException();
                        }
                )
                .bodyToMono(YoutubeCommentResponse.class)
                .doOnNext(response ->
                        log.info("비디오 {} - 페이지 토큰: {}, 댓글 개수: {}",
                                videoId, pageToken, response.getItems() != null ? response.getItems().size() : 0))
                .onErrorReturn(new YoutubeCommentResponse()); // 에러시 빈 응답 반환
    }

    /**
     * 비디오와 댓글을 함께 가져옴
     */
    public Mono<YoutubeVideoWithComments> getVideoWithComments(String videoId, Integer maxComments) {
        // 먼저 비디오 정보를 가져와야 하는 경우를 위해 비디오 정보 조회 메서드도 추가
        return getVideoDetails(videoId)
                .flatMap(video -> {
                    YoutubeVideoWithComments result = YoutubeVideoWithComments.fromYouTubeVideo(video);

                    return getVideoComments(videoId, maxComments)
                            .map(this::convertToCommentInfo)
                            .collectList()
                            .map(comments -> {
                                result.setComments(comments);
                                return result;
                            });
                });
    }

    /**
     * 비디오 상세 정보 조회
     */
    public Mono<YoutubeVideo> getVideoDetails(String videoId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/videos")
                        .queryParam("part", "snippet")
                        .queryParam("id", videoId)
                        .queryParam("key", config.getKey())
                        .build())
                .retrieve()
                .bodyToMono(YoutubeVideoListResponse.class)
                .map(response -> {
                    if (response.getItems() != null && !response.getItems().isEmpty()) {
                        return response.getItems().get(0);
                    }
                    throw new RuntimeException("비디오를 찾을 수 없습니다: " + videoId);
                });
    }

    /**
     * 채널의 모든 비디오와 댓글을 함께 가져옴
     */
    public Flux<YoutubeVideoWithComments> getAllVideosWithCommentsFromChannel(String channelId, Integer maxCommentsPerVideo) {
        return getAllVideosFromChannel(channelId)
                .flatMap(video -> {
                    String videoId = null;
                    if (video.getSnippet().getResourceId() != null) {
                        videoId = video.getSnippet().getResourceId().getVideoId();
                    } else {
                        videoId = video.getVideoId();
                    }

                    if (videoId == null) {
                        log.warn("비디오 ID를 찾을 수 없습니다: {}", video);
                        return Mono.empty();
                    }

                    YoutubeVideoWithComments result = YoutubeVideoWithComments.fromYouTubeVideo(video);

                    return getVideoComments(videoId, maxCommentsPerVideo)
                            .map(this::convertToCommentInfo)
                            .collectList()
                            .map(comments -> {
                                result.setComments(comments);
                                return result;
                            })
                            .onErrorReturn(result); // 댓글 가져오기 실패시 비디오 정보만 반환
                }, 2); // 동시 처리 제한 (API 제한 고려)
    }

    /**
     * CommentThread를 CommentInfo로 변환
     */
    private YoutubeVideoWithComments.CommentInfo convertToCommentInfo(CommentThread commentThread) {
        var comment = commentThread.getSnippet().getTopLevelComment().getSnippet();
        var commentInfo = new YoutubeVideoWithComments.CommentInfo();

        commentInfo.setCommentId(commentThread.getId());
        commentInfo.setAuthorName(comment.getAuthorDisplayName());
        commentInfo.setAuthorProfileImage(comment.getAuthorProfileImageUrl());
        commentInfo.setCommentText(comment.getTextDisplay());
        commentInfo.setLikeCount(comment.getLikeCount());
        commentInfo.setPublishedAt(comment.getPublishedAt());
        commentInfo.setUpdatedAt(comment.getUpdatedAt());
        commentInfo.setReplyCount(commentThread.getSnippet().getTotalReplyCount());

        return commentInfo;
    }

}
