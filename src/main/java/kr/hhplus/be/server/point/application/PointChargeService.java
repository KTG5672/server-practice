package kr.hhplus.be.server.point.application;

import kr.hhplus.be.server.point.domain.model.PointTransactionHistory;
import kr.hhplus.be.server.point.domain.model.TransactionType;
import kr.hhplus.be.server.point.domain.repository.PointTransactionHistoryRepository;
import kr.hhplus.be.server.user.domain.exception.UserNotFoundException;
import kr.hhplus.be.server.user.domain.model.User;
import kr.hhplus.be.server.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 유저의 포인트 충전 로직을 수행하는 비지니스 서비스
 */
@Service
public class PointChargeService {

    private final UserRepository userRepository;
    private final PointTransactionHistoryRepository pointTransactionHistoryRepository;

    public PointChargeService(UserRepository userRepository,
        PointTransactionHistoryRepository pointTransactionHistoryRepository) {
        this.userRepository = userRepository;
        this.pointTransactionHistoryRepository = pointTransactionHistoryRepository;
    }

    /**
     * 포인트 충전 메서드
     * - 유저 ID로 유저를 조회하고, 존재하지 않으면 UserNotFoundException 발생시킨다.
     * - 유저 도메인의 chargePoint 메서드를 호출하여 포인트를 충전한다.
     * - 변경된 유저 정보를 저장한다.
     * - 충전 내역을 저장한다.
     * @param userId 유저 ID
     * @param chargePoint 충전 포인트
     */
    @Transactional
    public void chargePoint(String userId, long chargePoint) {
        User user = userRepository.findById(userId).orElseThrow(() ->
            new UserNotFoundException(userId));
        user.chargePoint(chargePoint);
        userRepository.save(user);

        PointTransactionHistory pointTransactionHistory = PointTransactionHistory.of(userId,
            chargePoint, TransactionType.CHARGE);
        pointTransactionHistoryRepository.save(pointTransactionHistory);
    }

}
