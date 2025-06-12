package kr.hhplus.be.server.reservation.interfaces.gateway;

import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.reservation.entity.Reservation;
import kr.hhplus.be.server.reservation.entity.ReservationRepository;
import kr.hhplus.be.server.reservation.entity.ReservationStatus;
import org.springframework.stereotype.Component;

@Component
public class ReservationJpaGateway implements ReservationRepository {

    private final ReservationJpaDataRepository reservationJpaDataRepository;

    public ReservationJpaGateway(ReservationJpaDataRepository reservationJpaDataRepository) {
        this.reservationJpaDataRepository = reservationJpaDataRepository;
    }

    @Override
    public Reservation save(Reservation reservation) {
        return reservationJpaDataRepository.save(reservation);
    }

    @Override
    public List<Reservation> findBySeatId(Long seatId) {
        return reservationJpaDataRepository.findBySeatId(seatId);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservationJpaDataRepository.findById(id);
    }

    @Override
    public List<Reservation> findByStatus(ReservationStatus status) {
        return reservationJpaDataRepository.findByStatus(status);
    }

}
