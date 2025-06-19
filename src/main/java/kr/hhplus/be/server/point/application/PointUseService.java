package kr.hhplus.be.server.point.application;

import kr.hhplus.be.server.point.domain.model.PointTransactionHistory;
import kr.hhplus.be.server.point.domain.model.TransactionType;
import kr.hhplus.be.server.point.domain.repository.PointTransactionHistoryRepository;
import kr.hhplus.be.server.user.domain.exception.UserNotFoundException;
import kr.hhplus.be.server.user.domain.model.User;
import kr.hhplus.be.server.user.domain.repository.UserRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 포인트 사용 로직을 수행하는 비지니스 서비스
 * - 낙관적 락을 사용하여 포인트 사용요청 동시성 제어
 */
@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
public class PointUseService {

    private final UserRepository userRepository;
    private final PointTransactionHistoryRepository pointTransactionHistoryRepository;

    public PointUseService(UserRepository userRepository,
        PointTransactionHistoryRepository pointTransactionHistoryRepository) {
        this.userRepository = userRepository;
        this.pointTransactionHistoryRepository = pointTransactionHistoryRepository;
    }

    /**
     * 포인트 사용 메서드
     * - 유저 ID로 유저를 조회하고, 존재하지 않으면 UserNotFoundException 발생시킨다.
     * - 유저 도메인의 usePoint 메서드를 호출하여 포인트를 사용한다.
     * - 변경된 유저 정보를 저장한다.
     * - 사용 내역을 저장한다.
     * - 낙관전 락을 이용하여 동시성 제어 User.version, 재시도 3번
     * @param userId 유저 ID
     * @param usePoint 사용 포인트
     */
    @Retryable(
        retryFor = ObjectOptimisticLockingFailureException.class)
    public void usePoint(String userId, long usePoint) {
        User user = userRepository.findById(userId).orElseThrow(() ->
            new UserNotFoundException(userId));
        user.usePoint(usePoint);
        userRepository.save(user);

        PointTransactionHistory pointTransactionHistory = PointTransactionHistory.of(userId,
            usePoint, TransactionType.USE);
        pointTransactionHistoryRepository.save(pointTransactionHistory);
    }
}
