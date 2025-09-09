package crawler.api;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "youtube.api")
@Data
public class YoutubeApiConfig {
    private String key;
    private String baseUrl;
}
