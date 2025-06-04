package kr.hhplus.be.server.user.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.hhplus.be.server.point.domain.model.Point;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserPointTest {

    private static @NotNull User getNewTestUser() {
        return User.of("00000001", "test@email.com", "testPassword");
    }

    /**
     * 유저 생성 후 기본 포인트는 0인지 검증한다.
     */
    @Test
    @DisplayName("유저는 처음 생성시 기본 포인트는 0이다.")
    void 유저는_처음_생성시_기본_포인트는_0이다() {
        // given
        // when
        User user = getNewTestUser();
        // then
        Point point = user.getPoint();
        assertThat(point.getAmount()).isEqualTo(0);
    }

    /**
     * 유저의 충전 기능을 호출하면 기존 포인트와 충전 요청한 포인트가 합산되는지 검증한다. (초기 포인트 0)
     */
    @Test
    @DisplayName("유저가 포인트 충전시 기존 포인트와 합산된 포인트를 가진다.")
    void 유저가_포인트_충전시_기존_포인트와_합산된_포인트를_가진다() {
        // given
        User user = getNewTestUser();
        // when
        user.chargePoint(1_000L);
        // then
        Point point = user.getPoint();
        assertThat(point.getAmount()).isEqualTo(1_000L);
    }

    /**
     * 충전 요청된 포인트가 0이상이면 정상 수행되는지 검증한다.
     * - 충전 요청 포인트 0 검증
     */
    @Test
    @DisplayName("충전 포인트가 0 이면 정상적으로 충전이 된다.")
    void 충전_포인트가_0_이면_정상적으로_충전이_된다() {
        // given
        User user = getNewTestUser();
        // when
        user.chargePoint(0);
        // then
        Point point = user.getPoint();
        assertThat(point.getAmount()).isEqualTo(0);
    }

    /**
     * 충전 요청된 포인트가 0이상이면 정상 수행되는지 검증한다.
     * - 충전 요청 포인트 1 검증
     */
    @Test
    @DisplayName("충전 포인트가 1 이면 정상적으로 충전이 된다.")
    void 충전_포인트가_1_이면_정상적으로_충전이_된다() {
        // given
        User user = getNewTestUser();
        // when
        user.chargePoint(1);
        // then
        Point point = user.getPoint();
        assertThat(point.getAmount()).isEqualTo(1);
    }

    /**
     * 충전 요청된 포인트가 0미만이면 예외가 발생하는지 검증한다.
     */
    @Test
    @DisplayName("충전 포인트가 0미만이면 예외가 발생한다.")
    void 충전_포인트가_0미만이면_예외가_발생한다() {
        // given
        User user = getNewTestUser();
        // when
        var throwableAssert = assertThatThrownBy(() -> user.chargePoint(-1));
        // then
        throwableAssert.isInstanceOf(IllegalStateException.class);
    }


    /**
     * 유저의 포인트 사용 기능을 호출하면 기존 포인트에서 사용 요청한 포인트가 차감되는지 검증한다.
     */
    @Test
    @DisplayName("유저가 포인트 사용시 기존 포인트에서 차감된 포인트를 가진다.")
    void 유저가_포인트_사용시_기존_포인트에서_차감된_포인트를_가진다() {
        // given
        User user = getNewTestUser();
        user.chargePoint(1_000L);
        // when
        user.usePoint(1_000L);
        // then
        Point point = user.getPoint();
        assertThat(point.getAmount()).isEqualTo(0);
    }

    /**
     * 사용 요청된 포인트가 0이상이면 정상 수행되는지 검증한다.
     * - 기존 포인트 0, 사용 요청 포인트 0 검증
     */
    @Test
    @DisplayName("사용 포인트가 0 이면 정상적으로 사용이 된다.")
    void 사용_포인트가_0_이면_정상적으로_사용이_된다() {
        // given
        User user = getNewTestUser();
        // when
        user.usePoint(0);
        // then
        Point point = user.getPoint();
        assertThat(point.getAmount()).isEqualTo(0);
    }

    /**
     * 사용 요청된 포인트가 0이상이면 정상 수행되는지 검증한다.
     * - 충전 요청 포인트 1 검증
     */
    @Test
    @DisplayName("사용 포인트가 1 이면 정상적으로 사용이 된다.")
    void 사용_포인트가_1_이면_정상적으로_사용이_된다() {
        // given
        User user = getNewTestUser();
        user.chargePoint(1_000L);
        // when
        user.usePoint(1);
        // then
        Point point = user.getPoint();
        assertThat(point.getAmount()).isEqualTo(999L);
    }

    /**
     * 사용 요청된 포인트가 0미만이면 예외가 발생하는지 검증한다.
     */
    @Test
    @DisplayName("사용 포인트가 0미만이면 예외가 발생한다.")
    void 사용_포인트가_0미만이면_예외가_발생한다() {
        // given
        User user = getNewTestUser();
        // when
        var throwableAssert = assertThatThrownBy(() -> user.usePoint(-1));
        // then
        throwableAssert.isInstanceOf(IllegalStateException.class);
    }




}