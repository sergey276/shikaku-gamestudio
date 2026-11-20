package sk.tuke.gamestudio.service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import sk.tuke.gamestudio.entity.Rating;

public class RatingServiceJDBC implements RatingService {
    private static final String URL = env("DB_URL", "jdbc:postgresql://localhost:5432/gamestudio");
    private static final String USER = env("DB_USER", "postgres");
    private static final String PASSWORD = env("DB_PASSWORD", "");

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value != null ? value : fallback;
    }

    private static final String INSERT_RATING =
            "INSERT INTO rating (game, player, rating, rated_on) VALUES (?, ?, ?, ?)";

    private static final String UPDATE_RATING =
            "UPDATE rating SET rating = ?, rated_on = ? WHERE game = ? AND player = ?";

    private static final String SELECT_AVG_RATING =
            "SELECT AVG(rating) AS avg_rating FROM rating WHERE game = ?";

    private static final String SELECT_PLAYER_RATING =
            "SELECT rating FROM rating WHERE game = ? AND player = ?";

    private static final String SELECT_RATINGS =
            "SELECT game, player, rating FROM rating WHERE game = ? ORDER BY rated_on DESC";

    private static final String DELETE_RATINGS =
            "DELETE FROM rating";

    @Override
    public void setRating(String game, String player, int rating) throws RatingException {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            int existingRating = getRating(game, player);

            if (existingRating == 0) {
                try (PreparedStatement statement = connection.prepareStatement(INSERT_RATING)) {
                    statement.setString(1, game);
                    statement.setString(2, player);
                    statement.setInt(3, rating);
                    statement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                    statement.executeUpdate();
                }
            } else {
                try (PreparedStatement statement = connection.prepareStatement(UPDATE_RATING)) {
                    statement.setInt(1, rating);
                    statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                    statement.setString(3, game);
                    statement.setString(4, player);
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RatingException("Cannot set rating.", e);
        }
    }

    @Override
    public int getAverageRating(String game) throws RatingException {
        try (
                Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement statement = connection.prepareStatement(SELECT_AVG_RATING)
        ) {
            statement.setString(1, game);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("avg_rating");
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new RatingException("Cannot get average rating.", e);
        }
    }

    @Override
    public int getRating(String game, String player) throws RatingException {
        try (
                Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement statement = connection.prepareStatement(SELECT_PLAYER_RATING)
        ) {
            statement.setString(1, game);
            statement.setString(2, player);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("rating");
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new RatingException("Cannot get rating.", e);
        }
    }

    @Override
    public List<Rating> getRatings(String game) throws RatingException {
        try (
                Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement statement = connection.prepareStatement(SELECT_RATINGS)
        ) {
            statement.setString(1, game);

            try (ResultSet rs = statement.executeQuery()) {
                List<Rating> ratings = new ArrayList<>();
                while (rs.next()) {
                    ratings.add(new Rating(
                            rs.getString("game"),
                            rs.getString("player"),
                            rs.getInt("rating")
                    ));
                }
                return ratings;
            }
        } catch (SQLException e) {
            throw new RatingException("Cannot get ratings.", e);
        }
    }

    @Override
    public void reset() throws RatingException {
        try (
                Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate(DELETE_RATINGS);
        } catch (SQLException e) {
            throw new RatingException("Cannot reset ratings.", e);
        }
    }
}
