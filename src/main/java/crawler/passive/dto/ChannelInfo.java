package crawler.passive.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChannelInfo {
    private String accountNickname;
    private String profileImageUrl;
    private String totalViews;
    private String totalFollowers;
    private String totalContents;

    public ChannelInfo(String totalViews, String totalFollowers, String totalContents) {
        this.totalViews = totalViews;
        this.totalFollowers = totalFollowers;
        this.totalContents = totalContents;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s %s %s %s", accountNickname, profileImageUrl,
                totalViews, totalFollowers, totalContents);
    }
}
