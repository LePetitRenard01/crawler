package crawler;

import crawler.passive.YoutubeCrawler;

public class CrawlerTest {
    public static void main(String[] args) {
        YoutubeCrawler crawler = new YoutubeCrawler();

        try {
            System.out.println("크롤링 시작...");
            crawler.crawlChannel("GroveOwl", 1);
            System.out.println("크롤링 완료");

        } catch (Exception e) {
            System.err.println("에러 발생: " + e.getMessage());
            e.printStackTrace();
        } finally {
            crawler.webClient.close();
        }
    }
}