package kr.hhplus.be.server.concert.infrastructure.persistence;

import kr.hhplus.be.server.concert.entity.Concert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertJpaDataRepository extends JpaRepository<Concert, Long> {

}
