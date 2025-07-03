package kr.hhplus.be.server.payment.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.hhplus.be.server.payment.entity.Payment;
import kr.hhplus.be.server.payment.entity.PaymentRepository;
import kr.hhplus.be.server.payment.entity.PaymentStatus;
import kr.hhplus.be.server.payment.entity.exception.ProcessPaymentFailException;
import kr.hhplus.be.server.point.application.PointUseService;
import kr.hhplus.be.server.reservation.entity.Reservation;
import kr.hhplus.be.server.reservation.entity.ReservationHoldManager;
import kr.hhplus.be.server.reservation.entity.ReservationRepository;
import kr.hhplus.be.server.reservation.entity.exception.CannotPayReservationException;
import kr.hhplus.be.server.reservation.entity.exception.ReservationNotFoundException;
import kr.hhplus.be.server.user.domain.exception.NotEnoughPointException;
import kr.hhplus.be.server.user.domain.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ProcessPaymentServiceTest {

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    ReservationHoldManager reservationHoldManager;

    @Mock
    PointUseService pointUseService;

    @Mock
    ApplicationEventPublisher eventPublisher;

    ProcessPaymentService processPaymentService;
    PaymentFailHandler paymentFailHandler;

    @BeforeEach
    void setUp() {
        paymentFailHandler = new PaymentFailHandler(paymentRepository);
        processPaymentService = new ProcessPaymentService(paymentRepository, reservationRepository,
            paymentFailHandler, reservationHoldManager, pointUseService, eventPublisher);

    }

    /**
     * 에약 정보의 가격을 들고와 결제에서 사용되는지 검증한다.
     */
    @Test
    @DisplayName("결제시 예약 정보를 조회하여 가격을 구한다.")
    void 결제시_예약_정보를_조회해_가격을_구한다() {
        // given
        Long reservationId = 1L;
        String userId = "user-1";
        Long seatId = 3L;
        int price = 1000;

        when(reservationHoldManager.isValid(reservationId)).thenReturn(true);

        when(reservationRepository.findById(reservationId)).thenReturn(
            Optional.of(Reservation.holdOf(userId, seatId, price))
        );

        Payment mockPayment = Payment.processOf(userId, reservationId, price);
        when(paymentRepository.save(any())).thenReturn(mockPayment);

        ProcessPaymentCommand processPaymentCommand = new ProcessPaymentCommand(userId, reservationId);
        ArgumentCaptor<Payment> paymentArgumentCaptor = ArgumentCaptor.forClass(
            Payment.class);

        // when
        processPaymentService.processPayment(processPaymentCommand);
        // then
        verify(paymentRepository, times(1)).save(paymentArgumentCaptor.capture());
        Payment payment = paymentArgumentCaptor.getAllValues().get(0);
        assertEquals(price, payment.getAmount());
    }

    /**
     * 결제 시 예약 식별자를 이용하여 예약을 구할때 좌석이 없으면 ReservationNotFoundException 예외가 발생하는지 검증한다.
     */
    @Test
    @DisplayName("결제시 예약 정보가 없으면 예외가 발생한다")
    void 결제시_예약_정보가_없으면_예외가_발생한다() {
        // given
        Long reservationId = 1L;
        String userId = "user-1";

        when(reservationHoldManager.isValid(reservationId)).thenReturn(true);

        when(reservationRepository.findById(any())).thenReturn(Optional.empty());
        ProcessPaymentCommand processPaymentCommand = new ProcessPaymentCommand(userId, reservationId);

        // when
        var thrownBy = assertThatThrownBy(
            () -> processPaymentService.processPayment(processPaymentCommand));
        // then
        thrownBy.isInstanceOf(ReservationNotFoundException.class)
            .hasMessageContaining(reservationId.toString());
    }


    /**
     * 결제 시 유저 식별자를 이용하여 유저 조회시 없으면 ProcessPaymentFailException 예외가 발생하는지 검증한다.
     */
    @Test
    @DisplayName("결제시 유저 정보가 없으면 예외가 발생한다")
    void 결제시_유저_정보가_없으면_예외가_발생한다() {
        // given
        Long reservationId = 1L;
        String userId = "user-1";
        Long seatId = 3L;
        int price = 1000;

        when(reservationHoldManager.isValid(reservationId)).thenReturn(true);

        Reservation mockReservation = Reservation.holdOf(userId, seatId, price);
        when(reservationRepository.findById(reservationId)).thenReturn(
            Optional.of(mockReservation)
        );

        doThrow(UserNotFoundException.class).when(pointUseService).usePoint(userId, price);
        when(paymentRepository.save(any(Payment.class))).thenReturn(Payment.processOf(userId, reservationId, price));

        ProcessPaymentCommand processPaymentCommand = new ProcessPaymentCommand(userId, reservationId);

        // when
        var thrownBy = assertThatThrownBy(
            () -> processPaymentService.processPayment(processPaymentCommand));
        // then
        thrownBy.isInstanceOf(ProcessPaymentFailException.class);
    }

    /**
     * 결제 시 유저의 포인트가 결제금액 보다 부족하면 ProcessPaymentFailException 예외가 발생하는지 검증한다.
     */
    @Test
    @DisplayName("결제시 유저의 포인트가 부족하면 예외가 발생한다")
    void 결제시_유저의_포인트가_부족하면_예외가_발생한다() {
        // given
        Long reservationId = 1L;
        String userId = "user-1";
        Long seatId = 3L;
        int price = 4000;

        when(reservationHoldManager.isValid(reservationId)).thenReturn(true);
        when(reservationRepository.findById(reservationId)).thenReturn(
            Optional.of(Reservation.holdOf(userId, seatId, price))
        );

        Payment mockPayment = Payment.processOf(userId, reservationId, price);
        when(paymentRepository.save(any())).thenReturn(mockPayment);

        doThrow(NotEnoughPointException.class).when(pointUseService).usePoint(userId, price);

        ProcessPaymentCommand processPaymentCommand = new ProcessPaymentCommand(userId, reservationId);

        // when
        var thrownBy = assertThatThrownBy(
            () -> processPaymentService.processPayment(processPaymentCommand));
        // then
        thrownBy.isInstanceOf(ProcessPaymentFailException.class);
    }


    /**
     * 결제 성공 시 결제 상태는 SUCCESS로 변경 및 저장 되는지 검증한다.
     */
    @Test
    @DisplayName("결제 성공 시 결제상태는 SUCCESS로 변경된다.")
    void 결제_성공시_결제_상태는_SUCCESS_로_변경된다() {
        // given
        Long reservationId = 1L;
        String userId = "user-1";
        Long seatId = 3L;
        int price = 1000;

        when(reservationHoldManager.isValid(reservationId)).thenReturn(true);

        // 예약 정보 세팅
        when(reservationRepository.findById(reservationId)).thenReturn(
            Optional.of(Reservation.holdOf(userId, seatId, price))
        );

        Payment mockPayment = Payment.processOf(userId, reservationId, price);
        when(paymentRepository.save(any())).thenReturn(mockPayment);

        ProcessPaymentCommand processPaymentCommand = new ProcessPaymentCommand(userId, reservationId);
        ArgumentCaptor<Payment> paymentArgumentCaptor = ArgumentCaptor.forClass(
            Payment.class);

        // when
        processPaymentService.processPayment(processPaymentCommand);

        // then
        verify(paymentRepository, times(1)).save(paymentArgumentCaptor.capture());
        Payment payment = paymentArgumentCaptor.getAllValues().get(0);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }


    /**
     * 결제 성공 시 결제 상태는 FAIL로 변경 및 저장 되는지 검증한다.
     */
    @Test
    @DisplayName("결제 실패 시 결제상태는 FAIL로 변경된다.")
    void 결제_실패시_결제_상태는_FAIL_로_변경된다() {
        // given
        Long reservationId = 1L;
        String userId = "user-1";
        Long seatId = 3L;
        int price = 3000;

        when(reservationHoldManager.isValid(reservationId)).thenReturn(true);

        // 예약 정보 세팅
        when(reservationRepository.findById(reservationId)).thenReturn(
            Optional.of(Reservation.holdOf(userId, seatId, price))
        );

        Payment mockPayment = Payment.processOf(userId, reservationId, price);
        when(paymentRepository.save(any())).thenReturn(mockPayment);

        doThrow(UserNotFoundException.class).when(pointUseService).usePoint(userId, price);

        ProcessPaymentCommand processPaymentCommand = new ProcessPaymentCommand(userId, reservationId);
        ArgumentCaptor<Payment> paymentArgumentCaptor = ArgumentCaptor.forClass(
            Payment.class);

        // when
        assertThatThrownBy(() -> processPaymentService.processPayment(processPaymentCommand));

        // then
        verify(paymentRepository, times(1)).save(paymentArgumentCaptor.capture());
        Payment payment = paymentArgumentCaptor.getAllValues().get(0);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAIL);
    }


    /**
     * 결제 시 예약 상태가 HOLD(임시배정) 상태가 아니면 예외가 발생하는지 검증한다.
     */
    @Test
    @DisplayName("결제시 예약 상태가 HOLD가 아니면 예외가 발생한다.")
    void 결제시_예약_상태가_HOLD가_아니면_예외가_발생한다() {
        // given
        Long reservationId = 1L;
        String userId = "user-1";
        Long seatId = 3L;
        int price = 1000;

        when(reservationHoldManager.isValid(reservationId)).thenReturn(true);

        Reservation reservation = Reservation.holdOf(userId, seatId, price);
        reservation.completed();
        when(reservationRepository.findById(reservationId)).thenReturn(
            Optional.of(reservation)
        );

        ProcessPaymentCommand processPaymentCommand = new ProcessPaymentCommand(userId, reservationId);

        // when
        var thrownBy = assertThatThrownBy(
            () -> processPaymentService.processPayment(processPaymentCommand));
        // then
        thrownBy.isInstanceOf(CannotPayReservationException.class)
            .hasMessageContaining("COMPLETED");
    }

}