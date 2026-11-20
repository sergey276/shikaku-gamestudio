package sk.tuke.gamestudio.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import sk.tuke.gamestudio.entity.Score;

import java.util.Arrays;
import java.util.List;

public class ScoreServiceRestClient implements ScoreService {
    private final String url = "http://localhost:8080/api/score";

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void addScore(Score score) throws ScoreException {
        restTemplate.postForEntity(url, score, Score.class);
    }

    @Override
    public List<Score> getTopScores(String game) throws ScoreException {
        Score[] scores = restTemplate.getForEntity(url + "/" + game, Score[].class).getBody();
        return scores == null ? List.of() : Arrays.asList(scores);
    }

    @Override
    public Integer getBestScore(String game, String player, String difficulty) throws ScoreException {
        return restTemplate.getForEntity(
                url + "/" + game + "/" + player + "/best?difficulty=" + difficulty,
                Integer.class
        ).getBody();
    }

    @Override
    public void reset() throws ScoreException {
        throw new UnsupportedOperationException("Not supported via web service");
    }
}
