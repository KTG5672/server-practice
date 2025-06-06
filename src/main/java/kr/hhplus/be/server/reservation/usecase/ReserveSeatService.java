package kr.hhplus.be.server.reservation.usecase;

import java.util.List;
import kr.hhplus.be.server.reservation.entity.Reservation;
import kr.hhplus.be.server.reservation.entity.ReservationRepository;
import kr.hhplus.be.server.reservation.entity.exception.AlreadyReservedSeatException;
import org.springframework.stereotype.Service;

/**
 * 좌석 예약 서비스
 */
@Service
public class ReserveSeatService implements ReserveSeatUseCase {

    private final ReservationRepository reservationRepository;

    public ReserveSeatService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    /**
     * 좌석 예약 메서드
     * - 예약하려는 좌석에 활성된 예약이 있으면 AlreadyReservedSeatException 예외 발생
     * @param reserveSeatCommand 좌석 예약 입력
     */
    @Override
    public void reserveSeat(ReserveSeatCommand reserveSeatCommand) {
        String userId = reserveSeatCommand.userId();
        Long seatId = reserveSeatCommand.seatId();

        validateAlreadyReserved(seatId);
        Reservation holdReservation = Reservation.hold(userId, seatId);

        // @Todo
        Reservation saved = reservationRepository.save(holdReservation);

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
