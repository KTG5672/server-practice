package kr.hhplus.be.server.reservation.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.common.application.lock.LockManager;
import kr.hhplus.be.server.reservation.entity.Reservation;
import kr.hhplus.be.server.reservation.entity.ReservationHoldManager;
import kr.hhplus.be.server.reservation.entity.ReservationRepository;
import kr.hhplus.be.server.reservation.entity.ReservationStatus;
import kr.hhplus.be.server.reservation.entity.exception.AlreadyReservedSeatException;
import kr.hhplus.be.server.seat.entity.Seat;
import kr.hhplus.be.server.seat.entity.SeatRepository;
import kr.hhplus.be.server.seat.entity.exception.SeatNotFoundException;
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

    @Mock
    SeatRepository seatRepository;

    @Mock
    LockManager lockManager;

    @Mock
    ReservationHoldManager reservationHoldManager;

    ReserveSeatService reserveSeatService;


    @BeforeEach
    void setUp() {
        reserveSeatService = new ReserveSeatService(reservationRepository, seatRepository,
            lockManager, reservationHoldManager);
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
        int price = 1000;
        when(seatRepository.findById(seatId)).thenReturn(
            Optional.of(new Seat(seatId, 2L, "A", 1, price)));
        ReserveSeatCommand reserveSeatCommand = new ReserveSeatCommand(userId, seatId);
        ArgumentCaptor<Reservation> reservationArgumentCaptor = ArgumentCaptor.forClass(
            Reservation.class);

        when(reservationRepository.save(any(Reservation.class))).thenReturn(Reservation.holdOf(null, null, 0));

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
     * 예약시 좌석 식별자를 이용하여 가격을 구하는지 검증한다.
     */
    @Test
    @DisplayName("에약시 좌석 식별자를 이용하여 가격을 구한다.")
    void 예약시_좌석_식별자로_가격을_구한다() {
        // given
        Long seatId = 1L;
        String userId = "user-1";
        int price = 1000;
        when(seatRepository.findById(seatId)).thenReturn(
            Optional.of(new Seat(seatId, 2L, "A", 1, price)));
        ArgumentCaptor<Reservation> reservationArgumentCaptor = ArgumentCaptor.forClass(
            Reservation.class);
        ReserveSeatCommand reserveSeatCommand = new ReserveSeatCommand(userId, seatId);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(Reservation.holdOf(null, null, 0));
        // when
        reserveSeatService.reserveSeat(reserveSeatCommand);
        // then
        verify(reservationRepository).save(reservationArgumentCaptor.capture());
        Reservation result = reservationArgumentCaptor.getValue();
        assertThat(result.getPrice()).isEqualTo(price);
    }

    /**
     * 예약시 좌석 식별자를 이용하여 가격을 구할때 좌석이 없으면 SeatNotFoundException 예외가 발생하는지 검증한다.
     */
    @Test
    @DisplayName("에약시 좌석 식별자를 이용하여 좌석 정보를 얻을때 없으면 예외가 발생한다.")
    void 예약시_좌석_식별자로_좌석_정보를_얻을때_없으면_예외가_발생한다() {
        // given
        Long seatId = 1L;
        String userId = "user-1";
        when(seatRepository.findById(seatId)).thenReturn(Optional.empty());
        ReserveSeatCommand reserveSeatCommand = new ReserveSeatCommand(userId, seatId);
        // when
        var thrownBy = assertThatThrownBy(
            () -> reserveSeatService.reserveSeat(reserveSeatCommand));
        // then
        thrownBy.isInstanceOf(SeatNotFoundException.class)
            .hasMessageContaining(seatId.toString());
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
        int price = 1000;
        when(seatRepository.findById(seatId)).thenReturn(
            Optional.of(new Seat(seatId, 2L, "A", 1, price)));
        when(reservationRepository.findBySeatId(seatId)).thenReturn(
            List.of(
                Reservation.holdOf("user-2", seatId, 1000)
                , new Reservation(null, "user-3", seatId, ReservationStatus.CANCELLED, price)));
        ReserveSeatCommand reserveSeatCommand = new ReserveSeatCommand(userId, seatId);
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
        int price = 1000;
        when(seatRepository.findById(seatId)).thenReturn(
            Optional.of(new Seat(seatId, 2L, "A", 1, price)));
        when(reservationRepository.findBySeatId(seatId)).thenReturn(
            List.of(
                new Reservation(null, "user-2", seatId, ReservationStatus.CANCELLED, 1000)
                , new Reservation(null, "user-3", seatId, ReservationStatus.CANCELLED, 2000)));
        ReserveSeatCommand reserveSeatCommand = new ReserveSeatCommand(userId, seatId);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(Reservation.holdOf(null, null, 0));
        // when
        reserveSeatService.reserveSeat(reserveSeatCommand);
        // then
        verify(reservationRepository).save(any());
    }

    /**
     * 좌석 예약시 LockManager를 이용하여 Lock을 정상적으로 잠금/해제하는지 검증한다.
     */
    @Test
    @DisplayName("좌석 예약시 좌석 식별자를 key로 Lock을 사용한다.")
    void 좌석_예약시_좌석_식별자를_key로_Lock을_사용한다() {
        // given
        Long seatId = 1L;
        String userId = "user-1";
        int price = 1000;
        when(seatRepository.findById(seatId)).thenReturn(
            Optional.of(new Seat(seatId, 2L, "A", 1, price)));
        ReserveSeatCommand reserveSeatCommand = new ReserveSeatCommand(userId, seatId);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(Reservation.holdOf(null, null, 0));
        // when
        reserveSeatService.reserveSeat(reserveSeatCommand);

        // then
        verify(lockManager).lock("seat:" + seatId);
        verify(lockManager).unlock("seat:" + seatId);

    }

}