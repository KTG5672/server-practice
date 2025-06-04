package kr.hhplus.be.server.user.domain.infrastructure.persistence;

import java.util.Optional;
import kr.hhplus.be.server.user.domain.model.User;
import kr.hhplus.be.server.user.domain.repository.UserRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UserJpaRepository implements UserRepository {

    @Override
    public Optional<User> findById(String id) {
        return Optional.empty();
    }

    @Override
    public User save(User user) {
        return null;
    }
}
