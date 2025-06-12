package kr.hhplus.be.server.point.infrastructure.persistence;

import kr.hhplus.be.server.point.domain.model.PointTransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointTransactionHistoryJpaDataRepository extends
    JpaRepository<PointTransactionHistory, Long> {

}
