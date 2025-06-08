package kr.hhplus.be.server.user.domain.repository;

import java.util.Optional;
import kr.hhplus.be.server.user.domain.model.User;

public interface UserRepository {
    Optional<User> findById(String id);
    User save(User user);
}
