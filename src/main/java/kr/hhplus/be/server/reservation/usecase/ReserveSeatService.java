package kr.hhplus.be.server.reservation.usecase;

import java.util.List;
import kr.hhplus.be.server.common.application.lock.LockManager;
import kr.hhplus.be.server.reservation.entity.Reservation;
import kr.hhplus.be.server.reservation.entity.ReservationHoldManager;
import kr.hhplus.be.server.reservation.entity.ReservationRepository;
import kr.hhplus.be.server.reservation.entity.exception.AlreadyReservedSeatException;
import kr.hhplus.be.server.seat.entity.Seat;
import kr.hhplus.be.server.seat.entity.SeatRepository;
import kr.hhplus.be.server.seat.entity.exception.SeatNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 좌석 예약 서비스
 */
@Service
public class ReserveSeatService implements ReserveSeatUseCase {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final LockManager lockManager;
    private final ReservationHoldManager reservationHoldManager;

    public ReserveSeatService(ReservationRepository reservationRepository,
        SeatRepository seatRepository, LockManager lockManager,
        ReservationHoldManager reservationHoldManager) {
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
        this.lockManager = lockManager;
        this.reservationHoldManager = reservationHoldManager;
    }

    /**
     * 좌석 예약 메서드
     * - 예약 하려는 좌석이 없으면 SeatNotFoundException 예외 발생
     * - 예약하려는 좌석에 활성된 예약이 있으면 AlreadyReservedSeatException 예외 발생
     * - 예약 진행 시 LockManager를 이용하여 동시성 처리
     * - 임시 배정 상태를 따로 저장 후 유효시간을 5분
     * @param reserveSeatCommand 좌석 예약 입력
     */
    @Override
    public void reserveSeat(ReserveSeatCommand reserveSeatCommand) {
        String userId = reserveSeatCommand.userId();
        Long seatId = reserveSeatCommand.seatId();

        lockManager.lock("seat:" + seatId);
        try {
            int price = getSeatPrice(seatId);
            validateAlreadyReserved(seatId);
            Reservation holdReservation = Reservation.holdOf(userId, seatId, price);

            Reservation saved = reservationRepository.save(holdReservation);
            // 임시배정 상태를 저장(유효시간 5분)
            reservationHoldManager.hold(saved.getId());
        } finally {
            lockManager.unlock("seat:" + seatId);
        }

    }

    private int getSeatPrice(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
            .orElseThrow(() -> new SeatNotFoundException(seatId));
        return seat.getPrice();
    }

    private void validateAlreadyReserved(Long seatId) {
        List<Reservation> existingReservations = reservationRepository.findBySeatId(seatId).stream()
            .filter(Reservation::isActive)
            .toList();
        if (!existingReservations.isEmpty()) {
            throw new AlreadyReservedSeatException(seatId);
        }
    }

}
