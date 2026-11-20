package sk.tuke.gamestudio.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@NamedQueries({
        @NamedQuery(
                name = "Score.getTopScores",
                query = "SELECT s FROM Score s " +
                        "WHERE s.game = :game " +
                        "AND NOT EXISTS (" +
                        "    SELECT 1 FROM Score s2 " +
                        "    WHERE s2.game = s.game " +
                        "      AND s2.player = s.player " +
                        "      AND s2.difficulty = s.difficulty " +
                        "      AND (" +
                        "          s2.points > s.points " +
                        "          OR (s2.points = s.points AND s2.playedOn < s.playedOn) " +
                        "          OR (s2.points = s.points AND s2.playedOn = s.playedOn AND s2.ident < s.ident)" +
                        "      )" +
                        ") " +
                        "ORDER BY s.points DESC, s.playedOn ASC, s.ident ASC"
        ),
        @NamedQuery(
                name = "Score.getBestScoreForPlayer",
                query = "SELECT MAX(s.points) FROM Score s WHERE s.game = :game AND s.player = :player AND s.difficulty = :difficulty"
        ),
        @NamedQuery(
                name = "Score.resetScores",
                query = "DELETE FROM Score"
        )
})
public class Score implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int ident;

    private String game;
    private String player;
    private String difficulty;
    private int points;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "played_on")
    private Date playedOn;

    public Score() {
    }

    public Score(String game, String player, String difficulty, int points, Date playedOn) {
        this.game = game;
        this.player = player;
        this.difficulty = difficulty;
        this.points = points;
        this.playedOn = playedOn;
    }

    public int getIdent() {
        return ident;
    }

    public void setIdent(int ident) {
        this.ident = ident;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDifficultyLabel() {
        if ("easy".equalsIgnoreCase(difficulty)) {
            return "Easy";
        }
        if ("medium".equalsIgnoreCase(difficulty)) {
            return "Medium";
        }
        if ("hard".equalsIgnoreCase(difficulty)) {
            return "Hard";
        }
        return difficulty == null ? "" : difficulty;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public Date getPlayedOn() {
        return playedOn;
    }

    public void setPlayedOn(Date playedOn) {
        this.playedOn = playedOn;
    }
}
