package kr.hhplus.be.server.user.domain.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String userId) {
        super("Not found user with id: " + userId);
    }
}
