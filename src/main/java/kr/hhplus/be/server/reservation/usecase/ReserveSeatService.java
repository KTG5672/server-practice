package kr.hhplus.be.server.reservation.usecase;

import java.util.List;
import kr.hhplus.be.server.reservation.entity.Reservation;
import kr.hhplus.be.server.reservation.entity.ReservationHoldManager;
import kr.hhplus.be.server.reservation.entity.ReservationRepository;
import kr.hhplus.be.server.reservation.entity.exception.AlreadyReservedSeatException;
import kr.hhplus.be.server.seat.entity.Seat;
import kr.hhplus.be.server.seat.entity.SeatRepository;
import kr.hhplus.be.server.seat.entity.exception.SeatNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 좌석 예약 서비스
 */
@Service
@Transactional(isolation = Isolation.READ_COMMITTED, timeout = 10)
public class ReserveSeatService implements ReserveSeatUseCase {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final ReservationHoldManager reservationHoldManager;

    public ReserveSeatService(ReservationRepository reservationRepository,
        SeatRepository seatRepository, ReservationHoldManager reservationHoldManager) {
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
        this.reservationHoldManager = reservationHoldManager;
    }

    /**
     * 좌석 예약 메서드
     * - 예약 하려는 좌석이 없으면 SeatNotFoundException 예외 발생
     * - 예약하려는 좌석에 활성된 예약이 있으면 AlreadyReservedSeatException 예외 발생
     * - 좌석 조회시 비관적 락을 이용하여 동시성 처리
     * - 임시 배정 상태를 따로 저장 후 유효시간을 5분
     * @param reserveSeatCommand 좌석 예약 입력
     */
    @Override
    public Long reserveSeat(ReserveSeatCommand reserveSeatCommand) {

        String userId = reserveSeatCommand.userId();
        Long seatId = reserveSeatCommand.seatId();

        // 비관전 락으로 좌석 조회
        int price = getSeatPrice(seatId);
        validateAlreadyReserved(seatId);

        Reservation holdReservation = Reservation.holdOf(userId, seatId, price);
        Reservation saved = reservationRepository.save(holdReservation);
        Long reservationId = saved.getId();
        // 임시배정 상태를 저장(유효시간 5분)
        reservationHoldManager.hold(reservationId);

        return reservationId;
    }

    private int getSeatPrice(Long seatId) {
        Seat seat = seatRepository.findWithLockById(seatId)
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
