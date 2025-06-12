package kr.hhplus.be.server.point.infrastructure.persistence;

import kr.hhplus.be.server.point.domain.model.PointTransactionHistory;
import kr.hhplus.be.server.point.domain.repository.PointTransactionHistoryRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PointTransactionHistoryJpaRepository implements PointTransactionHistoryRepository {

    private final PointTransactionHistoryJpaDataRepository pointTransactionHistoryJpaDataRepository;

    public PointTransactionHistoryJpaRepository(
        PointTransactionHistoryJpaDataRepository pointTransactionHistoryJpaDataRepository) {
        this.pointTransactionHistoryJpaDataRepository = pointTransactionHistoryJpaDataRepository;
    }

    @Override
    public PointTransactionHistory save(PointTransactionHistory pointTransactionHistory) {
        return pointTransactionHistoryJpaDataRepository.save(pointTransactionHistory);
    }
}
