package kr.hhplus.be.server.user.infrastructure.persistence;

import kr.hhplus.be.server.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaDataRepository extends JpaRepository<User, String> {

}
