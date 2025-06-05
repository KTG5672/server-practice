package kr.hhplus.be.server.reservation.interfaces.gateway;

import java.util.List;
import kr.hhplus.be.server.reservation.entity.Reservation;
import kr.hhplus.be.server.reservation.entity.ReservationRepository;
import org.springframework.stereotype.Component;

@Component
public class ReservationJpaGateway implements ReservationRepository {

    @Override
    public Reservation save(Reservation reservation) {
        return null;
    }

    @Override
    public List<Reservation> findBySeatId(Long seatId) {
        return List.of();
    }
}
