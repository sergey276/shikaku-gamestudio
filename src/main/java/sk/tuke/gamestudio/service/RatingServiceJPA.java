package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.entity.Rating;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Transactional
public class RatingServiceJPA implements RatingService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void setRating(String game, String player, int rating) throws RatingException {
        try {
            Rating existing = entityManager.createQuery(
                            "SELECT r FROM Rating r WHERE r.game = :game AND r.player = :player",
                            Rating.class
                    )
                    .setParameter("game", game)
                    .setParameter("player", player)
                    .getSingleResult();

            existing.setRating(rating);
            entityManager.merge(existing);
        } catch (NoResultException e) {
            entityManager.persist(new Rating(game, player, rating));
        }
    }

    @Override
    public int getAverageRating(String game) throws RatingException {
        Double avg = entityManager.createNamedQuery("Rating.getAverageRating", Double.class)
                .setParameter("game", game)
                .getSingleResult();

        if (avg == null) {
            return 0;
        }
        return (int) Math.round(avg);
    }

    @Override
    public int getRating(String game, String player) throws RatingException {
        try {
            Integer rating = entityManager.createNamedQuery("Rating.getRating", Integer.class)
                    .setParameter("game", game)
                    .setParameter("player", player)
                    .getSingleResult();

            return rating == null ? 0 : rating;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public List<Rating> getRatings(String game) throws RatingException {
        return entityManager.createNamedQuery("Rating.getRatings", Rating.class)
                .setParameter("game", game)
                .getResultList();
    }

    @Override
    public void reset() throws RatingException {
        entityManager.createNamedQuery("Rating.resetRatings").executeUpdate();
    }
}
