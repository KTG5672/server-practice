package kr.hhplus.be.server.reservation.usecase;

import static org.mockito.Mockito.*;

import java.util.List;

import kr.hhplus.be.server.reservation.entity.Reservation;
import kr.hhplus.be.server.reservation.entity.ReservationHoldManager;
import kr.hhplus.be.server.reservation.entity.ReservationRepository;
import kr.hhplus.be.server.reservation.entity.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationExpirationServiceTest {

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    ReservationHoldManager reservationHoldManager;

    ReservationExpirationService reservationExpirationService;

    @BeforeEach
    void setUp() {
        reservationExpirationService = new ReservationExpirationService(reservationRepository, reservationHoldManager);
    }

    /**
     * 임시 배정인 예약이 유효하면 취소 처리 하는지 검증한다.
     */
    @Test
    @DisplayName("임시배정된 예약이 유효하지 않으면 예약을 취소한다.")
    void 임시배정된_예약이_유효하지_않으면_예약을_취소한다() {
        // given
        Reservation reservation = new Reservation(1L, "user-1", 1L, ReservationStatus.HOLD, 1000, null);
        when(reservationRepository.findByStatus(ReservationStatus.HOLD)).thenReturn(List.of(reservation));
        when(reservationHoldManager.isValid(reservation.getId())).thenReturn(false);

        // when
        reservationExpirationService.expireReservation();

        // then
        verify(reservationRepository).updateReservationStatusByIdAndStatus(anyLong(), any(ReservationStatus.class), any(ReservationStatus.class));
    }

    /**
     * 임시 배정인 예약이 유효하면 취소 처리 하지 않는지 검증한다.
     */
    @Test
    @DisplayName("임시배정된 예약이 유효하면 아무 작업도 하지 않는다.")
    void 임시배정된_예약이_유효하면_아무_작업도_하지_않는다() {
        // given
        Reservation reservation = new Reservation(1L, "user-1", 1L, ReservationStatus.HOLD, 1000, null);
        when(reservationRepository.findByStatus(ReservationStatus.HOLD)).thenReturn(List.of(reservation));
        when(reservationHoldManager.isValid(reservation.getId())).thenReturn(true);

        // when
        reservationExpirationService.expireReservation();

        // then
        verify(reservationRepository, never()).updateReservationStatusByIdAndStatus(any(), any(ReservationStatus.class), any(ReservationStatus.class));
    }
}