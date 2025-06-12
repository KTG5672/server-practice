package kr.hhplus.be.server.reservation.usecase;

import java.util.List;
import kr.hhplus.be.server.reservation.entity.Reservation;
import kr.hhplus.be.server.reservation.entity.ReservationHoldManager;
import kr.hhplus.be.server.reservation.entity.ReservationRepository;
import kr.hhplus.be.server.reservation.entity.ReservationStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 예약(임시배정) 만료 처리 서비스
 * - 만료 처리된 예약 건을 취소 처리
 */
@Service
@Transactional
public class ReservationExpirationService implements ReservationExpirationUseCase {

    private final ReservationRepository reservationRepository;
    private final ReservationHoldManager reservationHoldManager;

    public ReservationExpirationService(ReservationRepository reservationRepository,
        ReservationHoldManager reservationHoldManager) {
        this.reservationRepository = reservationRepository;
        this.reservationHoldManager = reservationHoldManager;
    }

    /**
     * 예약 만료 상태 처리 메서드
     * - 예약 상태가 임시배정인 상태 조회 후 만료 되었으면 취소 처리
     */
    @Override
    public void expireReservation() {
        // 예약 상태가 임시배정인 상태 조회
        List<Reservation> target = reservationRepository.findByStatus(
            ReservationStatus.HOLD);

        target.forEach(this::cancelNotValidReservation);
    }

    private void cancelNotValidReservation(Reservation reservation) {
        if (!reservationHoldManager.isValid(reservation.getId())) {
            reservation.cancelled();
            reservationRepository.save(reservation);
        }
    }
}
