//package crawler.passive;
//
//import crawler.passive.dto.ChannelInfo;
//import lombok.extern.slf4j.Slf4j;
//import org.htmlunit.BrowserVersion;
//import org.htmlunit.WebClient;
//import org.htmlunit.html.HtmlPage;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//@Component
//@Slf4j
//public class YoutubeCrawler {
//
//    public final WebClient webClient;
//
//    public YoutubeCrawler() {
//        this.webClient = new WebClient(BrowserVersion.CHROME);
//        webClient.getOptions().setJavaScriptEnabled(true);
//        webClient.getOptions().setCssEnabled(false);
//        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
//    }
//
//    public Document getDynamicPage(String url) throws IOException {
//        HtmlPage page = webClient.getPage(url);
//        webClient.waitForBackgroundJavaScript(5000);
//        return Jsoup.parse(page.asXml());
//    }
//
//    public String extractProfileImageUrl(Element pageHeader) {
//        if (pageHeader == null) return null;
//
//        // 여러 선택자로 시도
//        String[] selectors = {
//                "img.yt-spec-avatar-shape__image",
//                "img.ytCoreImageHost",
//                "img[src*='yt3.googleusercontent.com']",
//                "img[src*='googleusercontent.com']"
//        };
//
//        for (String selector : selectors) {
//            Element img = pageHeader.selectFirst(selector);
//            if (img != null) {
//                String src = img.attr("src");
//                if (!src.isEmpty()) {
//                    return src;
//                }
//            }
//        }
//
//        return null;
//    }
//
//    public String extractChannelName(Element pageHeader) {
//        if (pageHeader == null) return null;
//
//        String[] selectors = {
//                "h1.dynamicTextViewModelH1 span",
//                "h1 .yt-core-attributed-string",
//                "h1 span[role='text']",
//                "#page-header h1 span"
//        };
//
//        for (String selector : selectors) {
//            Element element = pageHeader.selectFirst(selector);
//            if (element != null && !element.text().trim().isEmpty()) {
//                return element.text().trim();
//            }
//        }
//
//        return null;
//    }
//
//    public ChannelInfo extractChannelInfo(Document aboutPage) {
//        Element aboutRenderer = aboutPage.selectFirst("ytd-about-channel-renderer");
//
//        ChannelInfo channelInfo = null;
//        if (aboutRenderer != null) {
//            // 총 조회수
//            Element viewElement = aboutRenderer.selectFirst("td:contains(조회수)");
//            String totalViews = viewElement != null ? viewElement.text() : "";
//
//            // 구독자 수
//            Element subscriberElement = aboutRenderer.selectFirst("td:contains(구독자)");
//            String subscribers = subscriberElement != null ? subscriberElement.text() : "";
//
//            // 동영상 개수
//            Element videoElement = aboutRenderer.selectFirst("td:contains(동영상)");
//            String videoCount = videoElement != null ? videoElement.text() : "";
//
//            channelInfo = new ChannelInfo(totalViews, subscribers, videoCount);
//        }
//
//        return channelInfo;
//    }
//
//        public void crawlChannel(String accountId, int categoryId) {
//        try {
//            //헤더
//            Document channelPage = getDynamicPage(String.format("https://www.youtube.com/@%s/videos", accountId));
//            Element pageHeader = channelPage.getElementById("page-header");
//            String accountProfileImageUrl = extractProfileImageUrl(pageHeader);
//            String accountNickname = extractChannelName(pageHeader);
//
//            //더보기
//            Document aboutPage = getDynamicPage(String.format("https://www.youtube.com/@%s/about", accountId));
//            ChannelInfo channelInfo = extractChannelInfo(aboutPage);
//            channelInfo.setAccountNickname(accountNickname);
//            channelInfo.setProfileImageUrl(accountProfileImageUrl);
//
//            System.out.println(channelInfo.toString());
//            //영상 리스트
//
//        } catch (Exception e) {
//            log.info("falied to get youtube info - account nickname : {}", accountId);
//        }
//
//    }
//}
