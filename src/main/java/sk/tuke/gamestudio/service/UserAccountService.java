package sk.tuke.gamestudio.service;

public interface UserAccountService {
    void register(String username, String password) throws UserAccountException;

    boolean authenticate(String username, String password) throws UserAccountException;

    boolean exists(String username) throws UserAccountException;

    void updatePassword(String username, String newPassword) throws UserAccountException;
}
