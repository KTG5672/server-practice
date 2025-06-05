package kr.hhplus.be.server.reservation.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import kr.hhplus.be.server.reservation.entity.Reservation;
import kr.hhplus.be.server.reservation.entity.ReservationRepository;
import kr.hhplus.be.server.reservation.entity.ReservationStatus;
import kr.hhplus.be.server.reservation.entity.exception.AlreadyReservedSeatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReserveSeatServiceTest {

    @Mock
    ReservationRepository reservationRepository;

    ReserveSeatService reserveSeatService;


    @BeforeEach
    void setUp() {
        reserveSeatService = new ReserveSeatService(reservationRepository);
    }

    /**
     * 좌석, 유저 식별자를 입력 받아 예약 정보를 정상적으로 저장하는지 검증한다.
     */
    @Test
    @DisplayName("좌석 식별자와 유저 식별자를 입력받아 예약 정보를 저장한다.")
    void 좌석_식별자와_유저_식별자를_입력받아_예약_정보를_저장한다() {
        // given
        Long seatId = 1L;
        String userId = "user-1";
        ReserveSeatCommand reserveSeatCommand = new ReserveSeatCommand(userId, seatId);
        ArgumentCaptor<Reservation> reservationArgumentCaptor = ArgumentCaptor.forClass(Reservation.class);

        // when
        reserveSeatService.reserveSeat(reserveSeatCommand);

        // then
        verify(reservationRepository).save(reservationArgumentCaptor.capture());
        Reservation result = reservationArgumentCaptor.getValue();
        assertThat(result.getSeatId()).isEqualTo(seatId);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.HOLD);
    }

    /**
     * 예약 하려는 좌석에 취소가 아닌 예약 정보가 있을 경우 AlreadyReservedSeatException 예외를 발생 시키는지 검증한다.
     */
    @Test
    @DisplayName("기존 좌석에 취소가 아닌 예약이 있을경우 예외를 발생시킨다.")
    void 기존_좌석에_취소가_아닌_예약이_있을경우_예외를_발생시킨다() {
        // given
        Long seatId = 1L;
        String userId = "user-1";
        ReserveSeatCommand reserveSeatCommand = new ReserveSeatCommand(userId, seatId);
        when(reservationRepository.findBySeatId(seatId)).thenReturn(
            List.of(
                Reservation.hold("user-2", seatId)
                , new Reservation(null, "user-3", seatId, ReservationStatus.CANCELLED)));
        // when
        var thrownBy = assertThatThrownBy(
            () -> reserveSeatService.reserveSeat(reserveSeatCommand));
        // then
        thrownBy.isInstanceOf(AlreadyReservedSeatException.class)
            .hasMessageContaining(seatId.toString());
    }

    /**
     * 예약 하려는 좌석에 취소된 예약 정보만 있을 경우 예외를 발생 시키는지 검증한다.
     */
    @Test
    @DisplayName("기존 좌석에 취소된 예약정보만 있을경우 정상 예약된다")
    void 기존_좌석에_취소된_예약정보만_있을경우_정상_예약된다() {
        // given
        Long seatId = 1L;
        String userId = "user-1";
        ReserveSeatCommand reserveSeatCommand = new ReserveSeatCommand(userId, seatId);
        when(reservationRepository.findBySeatId(seatId)).thenReturn(
            List.of(
                new Reservation(null, "user-2", seatId, ReservationStatus.CANCELLED)
                , new Reservation(null, "user-3", seatId, ReservationStatus.CANCELLED)));
        // when
        reserveSeatService.reserveSeat(reserveSeatCommand);
        // then
        verify(reservationRepository).save(any());
    }

}