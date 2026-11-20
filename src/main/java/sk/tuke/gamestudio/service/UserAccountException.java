package sk.tuke.gamestudio.service;

public class UserAccountException extends RuntimeException {
    public UserAccountException(String message) {
        super(message);
    }

    public UserAccountException(String message, Throwable cause) {
        super(message, cause);
    }
}
