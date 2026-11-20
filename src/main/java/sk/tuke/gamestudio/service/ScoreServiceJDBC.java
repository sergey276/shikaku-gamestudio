package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.entity.Score;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScoreServiceJDBC implements ScoreService {

    private static final String URL = env("DB_URL", "jdbc:postgresql://localhost:5432/gamestudio");
    private static final String USER = env("DB_USER", "postgres");
    private static final String PASSWORD = env("DB_PASSWORD", "");

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value != null ? value : fallback;
    }

    private static final String INSERT_SCORE =
            "INSERT INTO score (game, player, difficulty, points, played_on) VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_TOP_SCORES =
            "SELECT game, player, difficulty, points, played_on " +
            "FROM (" +
            "    SELECT game, player, difficulty, points, played_on, " +
            "           ROW_NUMBER() OVER (PARTITION BY player, difficulty ORDER BY points DESC, played_on ASC, id ASC) AS rn " +
            "    FROM score WHERE game = ?" +
            ") ranked " +
            "WHERE rn = 1 " +
            "ORDER BY points DESC, played_on ASC " +
            "LIMIT 100";

    private static final String SELECT_BEST_SCORE =
            "SELECT MAX(points) FROM score WHERE game = ? AND player = ? AND difficulty = ?";

    private static final String DELETE_SCORES =
            "DELETE FROM score";

    @Override
    public void addScore(Score score) throws ScoreException {
        try (
                Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement statement = connection.prepareStatement(INSERT_SCORE)
        ) {
            statement.setString(1, score.getGame());
            statement.setString(2, score.getPlayer());
            statement.setString(3, score.getDifficulty());
            statement.setInt(4, score.getPoints());
            statement.setTimestamp(5, new Timestamp(score.getPlayedOn().getTime()));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new ScoreException("Cannot add score.", e);
        }
    }

    @Override
    public List<Score> getTopScores(String game) throws ScoreException {
        try (
                Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement statement = connection.prepareStatement(SELECT_TOP_SCORES)
        ) {
            statement.setString(1, game);

            try (ResultSet rs = statement.executeQuery()) {
                List<Score> scores = new ArrayList<>();
                while (rs.next()) {
                    scores.add(new Score(
                            rs.getString("game"),
                            rs.getString("player"),
                            rs.getString("difficulty"),
                            rs.getInt("points"),
                            rs.getTimestamp("played_on")
                    ));
                }
                return scores;
            }
        } catch (SQLException e) {
            throw new ScoreException("Cannot get top scores.", e);
        }
    }

    @Override
    public Integer getBestScore(String game, String player, String difficulty) throws ScoreException {
        try (
                Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement statement = connection.prepareStatement(SELECT_BEST_SCORE)
        ) {
            statement.setString(1, game);
            statement.setString(2, player);
            statement.setString(3, difficulty);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    int bestScore = rs.getInt(1);
                    return rs.wasNull() ? null : bestScore;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new ScoreException("Cannot get player best score.", e);
        }
    }

    @Override
    public void reset() throws ScoreException {
        try (
                Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate(DELETE_SCORES);
        } catch (SQLException e) {
            throw new ScoreException("Cannot reset scores.", e);
        }
    }
}
