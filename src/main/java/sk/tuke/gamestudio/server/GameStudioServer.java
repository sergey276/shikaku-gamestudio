package sk.tuke.gamestudio.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import sk.tuke.gamestudio.service.CommentService;
import sk.tuke.gamestudio.service.CommentServiceJPA;
import sk.tuke.gamestudio.service.RatingService;
import sk.tuke.gamestudio.service.RatingServiceJPA;
import sk.tuke.gamestudio.service.ScoreService;
import sk.tuke.gamestudio.service.ScoreServiceJPA;
import sk.tuke.gamestudio.service.UserAccountService;
import sk.tuke.gamestudio.service.UserAccountServiceJPA;

import java.awt.Desktop;
import java.net.URI;

@SpringBootApplication
@Configuration
@EntityScan("sk.tuke.gamestudio.entity")
public class GameStudioServer {
    private static final String WEB_GAME_URL = "http://localhost:8080/shikaku";

    public static void main(String[] args) {
        SpringApplication.run(GameStudioServer.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void openGameInBrowser() {
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
    public ScoreService scoreService() {
        return new ScoreServiceJPA();
    }

    @Bean
    public CommentService commentService() {
        return new CommentServiceJPA();
    }

    @Bean
    public RatingService ratingService() {
        return new RatingServiceJPA();
    }

    @Bean
    public UserAccountService userAccountService() {
        return new UserAccountServiceJPA();
    }
}
