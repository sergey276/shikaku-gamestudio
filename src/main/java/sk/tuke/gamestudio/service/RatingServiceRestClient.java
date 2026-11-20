package sk.tuke.gamestudio.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import sk.tuke.gamestudio.entity.Rating;

import java.util.Arrays;
import java.util.List;

public class RatingServiceRestClient implements RatingService {
    private final String url = "http://localhost:8080/api/rating";

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void setRating(String game, String player, int rating) throws RatingException {
        restTemplate.postForEntity(url, new Rating(game, player, rating), Rating.class);
    }

    @Override
    public int getAverageRating(String game) throws RatingException {
        Integer value = restTemplate.getForEntity(url + "/" + game, Integer.class).getBody();
        return value == null ? 0 : value;
    }

    @Override
    public int getRating(String game, String player) throws RatingException {
        Integer value = restTemplate.getForEntity(url + "/" + game + "/" + player, Integer.class).getBody();
        return value == null ? 0 : value;
    }

    @Override
    public List<Rating> getRatings(String game) throws RatingException {
        Rating[] ratings = restTemplate.getForEntity(url + "/list/" + game, Rating[].class).getBody();
        return ratings == null ? List.of() : Arrays.asList(ratings);
    }

    @Override
    public void reset() throws RatingException {
        throw new UnsupportedOperationException("Not supported via web service");
    }
}
