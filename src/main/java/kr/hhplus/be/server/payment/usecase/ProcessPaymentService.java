package kr.hhplus.be.server.payment.usecase;

import kr.hhplus.be.server.common.lock.application.LockManager;
import kr.hhplus.be.server.payment.entity.Payment;
import kr.hhplus.be.server.payment.entity.PaymentRepository;
import kr.hhplus.be.server.payment.entity.exception.ProcessPaymentFailException;
import kr.hhplus.be.server.point.application.PointUseService;
import kr.hhplus.be.server.reservation.entity.Reservation;
import kr.hhplus.be.server.reservation.entity.ReservationHoldManager;
import kr.hhplus.be.server.reservation.entity.ReservationRepository;
import kr.hhplus.be.server.reservation.entity.ReservationStatus;
import kr.hhplus.be.server.reservation.entity.exception.CannotPayReservationException;
import kr.hhplus.be.server.reservation.entity.exception.ReservationNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 결제 프로세스 서비스
 */
@Service
@Transactional
public class ProcessPaymentService implements ProcessPaymentUseCase {

    public static final String PAYMENT_LOCK_PREFIX = "payment:";
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentFailHandler paymentFailHandler;
    private final LockManager lockManager;
    private final ReservationHoldManager reservationHoldManager;
    private final PointUseService pointUseService;

    public ProcessPaymentService(PaymentRepository paymentRepository,
        ReservationRepository reservationRepository, PaymentFailHandler paymentFailHandler,
        LockManager lockManager, ReservationHoldManager reservationHoldManager,
        PointUseService pointUseService) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.paymentFailHandler = paymentFailHandler;
        this.lockManager = lockManager;
        this.reservationHoldManager = reservationHoldManager;
        this.pointUseService = pointUseService;
    }

    /**
     * 결제 프로세스 기능
     * - 예약/유저 미존재 시 예외 발생
     * - PROCESS 결제 상태 생성 저장 -> 성공/실패 여부에 따라 상태 갱신
     * - 결제 실패 (포인트 부족 등) 시 예외 발생 (ProcessPaymentFailException)
     * - 결제 성공 시 포인트 차감, 예약 상태 변경
     * - 결제 진행 시 LockManager를 이용하여 동시성 처리
     * - 결제 진행 전 임시배정 상태 확인
     * @param processPaymentCommand 결제 진행 입력 값
     */
    @Override
    public void processPayment(ProcessPaymentCommand processPaymentCommand) {
        Long reservationId = processPaymentCommand.reservationId();
        String userId = processPaymentCommand.userId();

        // Redis 에서 임시배정 만료 확인
        validateReservationExpired(reservationId);

        lockManager.lock(PAYMENT_LOCK_PREFIX + userId);
        try {
            // 1. 필요 정보 조회 및 검증
            Reservation reservation = getReservation(reservationId);
            int price = reservation.getPrice();
            validateReservationStatus(reservation);

            // 2. 결제 상태 진행중으로 저장
            Payment saved = createAndSaveProcessingPayment(userId, reservationId, price);

            // 3. 결제
            try {
                pointUseService.usePoint(userId, price);
            } catch (RuntimeException e) {
                // 실패 시 상태(FAIL) 업데이트
                paymentFailHandler.handlePaymentFailed(saved);
                throw new ProcessPaymentFailException(saved.getId(), e.getMessage());
            }

            // 4. 결제 성공 로직
            handlePaymentSuccess(saved);
            completeReservation(reservation);
        } finally {
            lockManager.unlock(PAYMENT_LOCK_PREFIX + userId);
        }
    }

    private void validateReservationExpired(Long reservationId) {
        if (!reservationHoldManager.isValid(reservationId)) {
            throw new CannotPayReservationException(reservationId, "reservation is expired.");
        }
    }

    private Payment createAndSaveProcessingPayment(String userId, Long reservationId, int price) {
        Payment payment = Payment.processOf(userId, reservationId, price);
        return paymentRepository.save(payment);
    }

    private void validateReservationStatus(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.HOLD) {
            throw new CannotPayReservationException(reservation.getId(), reservation.getStatus());
        }
    }

    private void completeReservation(Reservation reservation) {
        reservation.completed();
        reservationRepository.save(reservation);
    }

    private void handlePaymentSuccess(Payment payment) {
        payment.success();
        paymentRepository.save(payment);
    }

    private Reservation getReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ReservationNotFoundException(reservationId));
    }

}
