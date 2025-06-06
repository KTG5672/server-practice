package kr.hhplus.be.server.payment.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.hhplus.be.server.payment.entity.Payment;
import kr.hhplus.be.server.payment.entity.PaymentRepository;
import kr.hhplus.be.server.payment.entity.PaymentStatus;
import kr.hhplus.be.server.payment.entity.exception.ProcessPaymentFailException;
import kr.hhplus.be.server.reservation.entity.Reservation;
import kr.hhplus.be.server.reservation.entity.ReservationRepository;
import kr.hhplus.be.server.reservation.entity.exception.ReservationNotFoundException;
import kr.hhplus.be.server.user.domain.exception.UserNotFoundException;
import kr.hhplus.be.server.user.domain.model.User;
import kr.hhplus.be.server.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessPaymentServiceTest {

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    UserRepository userRepository;

    ProcessPaymentService processPaymentService;

    @BeforeEach
    void setUp() {
        processPaymentService = new ProcessPaymentService(paymentRepository, reservationRepository,
            userRepository);
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

        when(reservationRepository.findById(reservationId)).thenReturn(
            Optional.of(Reservation.holdOf(userId, seatId, price))
        );

        User user = User.of(userId, "test@test.com", "1234");
        user.chargePoint(2_000);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Payment mockPayment = Payment.processOf(userId, reservationId, price);
        when(paymentRepository.save(any())).thenReturn(mockPayment);

        ProcessPaymentCommand processPaymentCommand = new ProcessPaymentCommand(userId, reservationId);
        ArgumentCaptor<Payment> paymentArgumentCaptor = ArgumentCaptor.forClass(
            Payment.class);

        // when
        processPaymentService.processPayment(processPaymentCommand);
        // then
        verify(paymentRepository, times(2)).save(paymentArgumentCaptor.capture());
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
     * 유저 정보의 결제에서 사용되는지 검증한다.
     */
    @Test
    @DisplayName("결제시 유저 정보를 조회하여 포인트를 차감한다")
    void 결제시_유저_정보를_조회해_포인트를_차감한다() {
        // given
        Long reservationId = 1L;
        String userId = "user-1";
        Long seatId = 3L;
        int price = 1000;

        when(reservationRepository.findById(reservationId)).thenReturn(
            Optional.of(Reservation.holdOf(userId, seatId, price))
        );

        User user = User.of(userId, "test@test.com", "1234");
        user.chargePoint(2_000);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Payment mockPayment = Payment.processOf(userId, reservationId, price);
        when(paymentRepository.save(any())).thenReturn(mockPayment);

        ProcessPaymentCommand processPaymentCommand = new ProcessPaymentCommand(userId, reservationId);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(
            User.class);

        // when
        processPaymentService.processPayment(processPaymentCommand);
        // then
        verify(userRepository).save(userArgumentCaptor.capture());
        User resultUser = userArgumentCaptor.getAllValues().get(0);
        assertEquals(1_000, resultUser.getPoint().getAmount());
    }

    /**
     * 결제 시 유저 식별자를 이용하여 유저 조회시 없으면 UserNotFoundException 예외가 발생하는지 검증한다.
     */
    @Test
    @DisplayName("결제시 유저 정보가 없으면 예외가 발생한다")
    void 결제시_유저_정보가_없으면_예외가_발생한다() {
        // given
        Long reservationId = 1L;
        String userId = "user-1";
        Long seatId = 3L;
        int price = 1000;

        when(reservationRepository.findById(reservationId)).thenReturn(
            Optional.of(Reservation.holdOf(userId, seatId, price))
        );

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ProcessPaymentCommand processPaymentCommand = new ProcessPaymentCommand(userId, reservationId);

        // when
        var thrownBy = assertThatThrownBy(
            () -> processPaymentService.processPayment(processPaymentCommand));
        // then
        thrownBy.isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining(userId);
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

        when(reservationRepository.findById(reservationId)).thenReturn(
            Optional.of(Reservation.holdOf(userId, seatId, price))
        );

        User user = User.of(userId, "test@test.com", "1234");
        user.chargePoint(3_000);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Payment mockPayment = Payment.processOf(userId, reservationId, price);
        when(paymentRepository.save(any())).thenReturn(mockPayment);
        ProcessPaymentCommand processPaymentCommand = new ProcessPaymentCommand(userId, reservationId);

        // when
        var thrownBy = assertThatThrownBy(
            () -> processPaymentService.processPayment(processPaymentCommand));
        // then
        thrownBy.isInstanceOf(ProcessPaymentFailException.class)
            .hasMessageContaining(price + "")
            .hasMessageContaining(3000 + "");
    }

    /**
     * 예약, 유저 식별자를 입력 받아 결제 정보(진행중)를 정상적으로 저장하는지 검증한다.
     */
    @Test
    @DisplayName("예약 식별자와 유저 식별자를 입력받아 진행중 상태의 결제 정보를 저장한다.")
    void 예약_식별자와_유저_식별자를_입력받아_진행중_상태의_결제_정보를_저장한다() {
        // given
        Long reservationId = 1L;
        String userId = "user-1";
        Long seatId = 3L;
        int price = 1000;

        // 예약 정보 세팅
        when(reservationRepository.findById(reservationId)).thenReturn(
            Optional.of(Reservation.holdOf(userId, seatId, price))
        );

        // 유저 정보 세팅
        User user = User.of(userId, "test@test.com", "1234");
        user.chargePoint(2_000);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Payment mockPayment = Payment.processOf(userId, reservationId, price);
        when(paymentRepository.save(any())).thenReturn(mockPayment);

        ProcessPaymentCommand processPaymentCommand = new ProcessPaymentCommand(userId, reservationId);
        ArgumentCaptor<Payment> paymentArgumentCaptor = ArgumentCaptor.forClass(
            Payment.class);

        // when
        processPaymentService.processPayment(processPaymentCommand);

        // then
        verify(paymentRepository, times(2)).save(paymentArgumentCaptor.capture());
        Payment payment = paymentArgumentCaptor.getAllValues().get(0);
        assertEquals(reservationId, payment.getReservationId());
        assertEquals(userId, payment.getUserId());
        assertThat(payment.getStats()).isEqualTo(PaymentStatus.PROCESS);
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

        // 예약 정보 세팅
        when(reservationRepository.findById(reservationId)).thenReturn(
            Optional.of(Reservation.holdOf(userId, seatId, price))
        );

        // 유저 정보 세팅
        User user = User.of(userId, "test@test.com", "1234");
        user.chargePoint(2_000);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Payment mockPayment = Payment.processOf(userId, reservationId, price);
        when(paymentRepository.save(any())).thenReturn(mockPayment);

        ProcessPaymentCommand processPaymentCommand = new ProcessPaymentCommand(userId, reservationId);
        ArgumentCaptor<Payment> paymentArgumentCaptor = ArgumentCaptor.forClass(
            Payment.class);

        // when
        processPaymentService.processPayment(processPaymentCommand);

        // then
        verify(paymentRepository, times(2)).save(paymentArgumentCaptor.capture());
        Payment payment = paymentArgumentCaptor.getAllValues().get(1);
        assertThat(payment.getStats()).isEqualTo(PaymentStatus.SUCCESS);
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

        // 예약 정보 세팅
        when(reservationRepository.findById(reservationId)).thenReturn(
            Optional.of(Reservation.holdOf(userId, seatId, price))
        );

        // 유저 정보 세팅
        User user = User.of(userId, "test@test.com", "1234");
        user.chargePoint(2_000);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Payment mockPayment = Payment.processOf(userId, reservationId, price);
        when(paymentRepository.save(any())).thenReturn(mockPayment);

        ProcessPaymentCommand processPaymentCommand = new ProcessPaymentCommand(userId, reservationId);
        ArgumentCaptor<Payment> paymentArgumentCaptor = ArgumentCaptor.forClass(
            Payment.class);

        // when
        assertThatThrownBy(() -> processPaymentService.processPayment(processPaymentCommand));

        // then
        verify(paymentRepository, times(2)).save(paymentArgumentCaptor.capture());
        Payment payment = paymentArgumentCaptor.getAllValues().get(1);
        assertThat(payment.getStats()).isEqualTo(PaymentStatus.FAIL);
    }



}