package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.entity.UserAccount;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Transactional
public class UserAccountServiceJPA implements UserAccountService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void register(String username, String password) throws UserAccountException {
        if (findByUsername(username) != null) {
            throw new UserAccountException("Username already exists.");
        }

        entityManager.persist(new UserAccount(username, hashPassword(password)));
    }

    @Override
    public boolean authenticate(String username, String password) throws UserAccountException {
        UserAccount userAccount = findByUsername(username);
        return userAccount != null && userAccount.getPasswordHash().equals(hashPassword(password));
    }

    @Override
    public boolean exists(String username) throws UserAccountException {
        return findByUsername(username) != null;
    }

    @Override
    public void updatePassword(String username, String newPassword) throws UserAccountException {
        UserAccount userAccount = findByUsername(username);
        if (userAccount == null) {
            throw new UserAccountException("This account is not registered yet. Please register first.");
        }

        userAccount.setPasswordHash(hashPassword(newPassword));
        entityManager.merge(userAccount);
    }

    private UserAccount findByUsername(String username) {
        try {
            return entityManager.createNamedQuery("UserAccount.getByUsername", UserAccount.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new UserAccountException("Password hashing is unavailable.", e);
        }
    }
}
