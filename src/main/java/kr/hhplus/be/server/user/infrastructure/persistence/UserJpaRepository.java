package kr.hhplus.be.server.user.infrastructure.persistence;

import java.util.Optional;
import kr.hhplus.be.server.user.domain.model.User;
import kr.hhplus.be.server.user.domain.repository.UserRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UserJpaRepository implements UserRepository {

    private final UserJpaDataRepository userJpaDataRepository;

    public UserJpaRepository(UserJpaDataRepository userJpaDataRepository) {
        this.userJpaDataRepository = userJpaDataRepository;
    }

    @Override
    public Optional<User> findById(String id) {
        return userJpaDataRepository.findById(id);
    }

    @Override
    public User save(User user) {
        return userJpaDataRepository.save(user);
    }
}
