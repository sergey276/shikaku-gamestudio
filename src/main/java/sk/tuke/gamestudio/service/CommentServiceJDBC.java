package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.entity.Comment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentServiceJDBC implements CommentService {
    private static final String URL = env("DB_URL", "jdbc:postgresql://localhost:5432/gamestudio");
    private static final String USER = env("DB_USER", "postgres");
    private static final String PASSWORD = env("DB_PASSWORD", "");

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value != null ? value : fallback;
    }

    private static final String INSERT_COMMENT =
            "INSERT INTO comment (game, player, comment, commented_on) VALUES (?, ?, ?, ?)";

    private static final String SELECT_COMMENTS =
            "SELECT game, player, comment, commented_on FROM comment WHERE game = ? ORDER BY commented_on DESC";

    private static final String DELETE_COMMENTS =
            "DELETE FROM comment";

    @Override
    public void addComment(Comment comment) throws CommentException {
        try (
                Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement statement = connection.prepareStatement(INSERT_COMMENT)
        ) {
            statement.setString(1, comment.getGame());
            statement.setString(2, comment.getPlayer());
            statement.setString(3, comment.getComment());
            statement.setTimestamp(4, new Timestamp(comment.getCommentedOn().getTime()));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new CommentException("Cannot add comment.", e);
        }
    }

    @Override
    public List<Comment> getComments(String game) throws CommentException {
        try (
                Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement statement = connection.prepareStatement(SELECT_COMMENTS)
        ) {
            statement.setString(1, game);

            try (ResultSet rs = statement.executeQuery()) {
                List<Comment> comments = new ArrayList<>();
                while (rs.next()) {
                    comments.add(new Comment(
                            rs.getString("game"),
                            rs.getString("player"),
                            rs.getString("comment"),
                            rs.getTimestamp("commented_on")
                    ));
                }
                return comments;
            }
        } catch (SQLException e) {
            throw new CommentException("Cannot get comments.", e);
        }
    }

    @Override
    public void reset() throws CommentException {
        try (
                Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate(DELETE_COMMENTS);
        } catch (SQLException e) {
            throw new CommentException("Cannot reset comments.", e);
        }
    }
}