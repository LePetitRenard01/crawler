package crawler.api;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final YoutubeApiConfig config;

    @Bean
    public WebClient youtubeWebClient() {
        return WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .build();
    }
}
