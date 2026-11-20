package sk.tuke.gamestudio;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.client.RestTemplate;
import sk.tuke.gamestudio.service.CommentService;
import sk.tuke.gamestudio.service.CommentServiceRestClient;
import sk.tuke.gamestudio.service.RatingService;
import sk.tuke.gamestudio.service.RatingServiceRestClient;
import sk.tuke.gamestudio.service.ScoreService;
import sk.tuke.gamestudio.service.ScoreServiceRestClient;

import java.awt.Desktop;
import java.net.URI;

@ComponentScan(excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "sk.tuke.gamestudio.server.*"
))
public class SpringClient {
    private static final String WEB_GAME_URL = "http://localhost:8080/shikaku";

    public static void main(String[] args) {
        var context = new SpringApplicationBuilder(SpringClient.class)
                .web(WebApplicationType.NONE)
                .run(args);

        openGameInBrowser();
        context.close();
    }

    private static void openGameInBrowser() {
        try {
            URI uri = URI.create(WEB_GAME_URL);
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(uri);
            } else {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + WEB_GAME_URL);
            }
        } catch (Exception e) {
            System.out.println("Open this URL in browser: " + WEB_GAME_URL);
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ScoreService scoreService() {
        return new ScoreServiceRestClient();
    }

    @Bean
    public CommentService commentService() {
        return new CommentServiceRestClient();
    }

    @Bean
    public RatingService ratingService() {
        return new RatingServiceRestClient();
    }

}
