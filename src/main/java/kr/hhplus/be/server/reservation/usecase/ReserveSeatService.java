package kr.hhplus.be.server.reservation.usecase;

import java.util.List;
import kr.hhplus.be.server.reservation.entity.Reservation;
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

    public ReserveSeatService(ReservationRepository reservationRepository,
        SeatRepository seatRepository) {
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
    }

    /**
     * 좌석 예약 메서드
     * - 예약 하려는 좌석이 없으면 SeatNotFoundException 예외 발생
     * - 예약하려는 좌석에 활성된 예약이 있으면 AlreadyReservedSeatException 예외 발생
     * @param reserveSeatCommand 좌석 예약 입력
     */
    @Override
    public void reserveSeat(ReserveSeatCommand reserveSeatCommand) {
        String userId = reserveSeatCommand.userId();
        Long seatId = reserveSeatCommand.seatId();
        int price = getSeatPrice(seatId);

        validateAlreadyReserved(seatId);
        Reservation holdReservation = Reservation.holdOf(userId, seatId, price);

        // @Todo
        Reservation saved = reservationRepository.save(holdReservation);

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
