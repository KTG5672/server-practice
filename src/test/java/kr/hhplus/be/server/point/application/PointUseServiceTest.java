package kr.hhplus.be.server.point.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.hhplus.be.server.point.domain.model.PointTransactionHistory;
import kr.hhplus.be.server.point.domain.model.TransactionType;
import kr.hhplus.be.server.point.domain.repository.PointTransactionHistoryRepository;
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
class PointUseServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PointTransactionHistoryRepository pointTransactionHistoryRepository;

    PointUseService pointUseService;

    @BeforeEach
    void setUp() {
        pointUseService = new PointUseService(userRepository, pointTransactionHistoryRepository);
    }

    /**
     * 포인트 사용 기능 호출 시 유저의 포인트를 차감 시키고, 실제 저장 하는지 검증한다.
     */
    @Test
    @DisplayName("포인트 사용 기능 호출시 포인트를 사용하고 차감된 포인트를 저장한다.")
    void 포인트_사용_기능_호출시_포인트를_충전하고_차감된포인트를_저장한다() {
        // given
        User user = User.of("0000001", "test@test.com", "1234");
        user.chargePoint(2_000L);
        long usePoint = 1_000L;
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // when
        pointUseService.usePoint(user.getId(), usePoint);

        // then
        assertThat(user.getPoint().getAmount()).isEqualTo(1_000L);
        verify(userRepository).save(user);
    }


    /**
     * 포인트 사용 기능은 유저 ID를 받아 DB 에서 유저 정보를 찾는데,
     * 이때 없을 시 예외가 발생하는지 검증한다.
     */
    @Test
    @DisplayName("포인트 사용 기능 호출시 없는 유저일 경우 예외가 발생한다.")
    void 포인트_사용_기능_호출시_없는_유저일_경우_예외가_발생한다() {
        // given
        String userId = "0000002";
        long useAmount = 1_000L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when
        var throwableAssert = assertThatThrownBy(
            () -> pointUseService.usePoint(userId, useAmount));

        // then
        throwableAssert
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining(userId);
    }

    /**
     * 포인트 사용 기능을 호출시 사용 내역을 정상적으로 저장하는지 검증한다.
     */
    @Test
    @DisplayName("포인트 사용시 사용 내역을 저장한다.")
    void 포인트_사용시_사용_내역을_저장한다() {
        // given
        User user = User.of("0000001", "test@test.com", "1234");
        user.chargePoint(2_000L);
        long useAmount = 1_000L;
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        ArgumentCaptor<PointTransactionHistory> captor = ArgumentCaptor.forClass(PointTransactionHistory.class);

        // when
        pointUseService.usePoint(user.getId(), useAmount);

        // then
        verify(pointTransactionHistoryRepository).save(captor.capture());
        PointTransactionHistory saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(user.getId());
        assertThat(saved.getAmount()).isEqualTo(useAmount);
        assertThat(saved.getType()).isEqualTo(TransactionType.USE);
    }
}