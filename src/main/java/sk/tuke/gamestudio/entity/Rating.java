package sk.tuke.gamestudio.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@NamedQueries({
        @NamedQuery(
                name = "Rating.getAverageRating",
                query = "SELECT AVG(r.rating) FROM Rating r WHERE r.game = :game"
        ),
        @NamedQuery(
                name = "Rating.getRating",
                query = "SELECT r.rating FROM Rating r WHERE r.game = :game AND r.player = :player"
        ),
        @NamedQuery(
                name = "Rating.getRatings",
                query = "SELECT r FROM Rating r WHERE r.game = :game ORDER BY r.ratedOn DESC"
        ),
        @NamedQuery(
                name = "Rating.setRating",
                query = "UPDATE Rating r SET r.rating = :rating WHERE r.game = :game AND r.player = :player"
        ),
        @NamedQuery(
                name = "Rating.resetRatings",
                query = "DELETE FROM Rating"
        )
})
public class Rating implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int ident;

    private String game;
    private String player;
    private int rating;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "rated_on")
    private Date ratedOn;

    public Rating() {
    }

    public Rating(String game, String player, int rating) {
        this.game = game;
        this.player = player;
        this.rating = rating;
        this.ratedOn = new Date();
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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Date getRatedOn() {
        return ratedOn;
    }

    public void setRatedOn(Date ratedOn) {
        this.ratedOn = ratedOn;
    }
}
