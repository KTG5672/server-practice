package kr.hhplus.be.server.point.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PointTest {

    /**
     * 포인트는 항상 0이상이어야 한다
     * 포인트가 0미만일 경우 예외를 발생 시키는지 검증
     */
    @Test
    @DisplayName("포인트가 0미만일 경우 예외를 발생시킨다.")
    void 포인트가_0미만일_경우_예외를_발생시킨다() {
        // given
        // when
        var throwableAssert = assertThatThrownBy(() -> new Point(-1));
        // then
        throwableAssert.isInstanceOf(IllegalStateException.class);
    }

    /**
     * 포인트를 합산하면 합산된 포인트로 Point 객체를 생성하여 정상적으로 반환하는지 검증
     */
    @Test
    @DisplayName("포인트를 합산하면 합산된 포인트로 새로운 객체를 반환한다.")
    void 포인트를_합산하면_합산된_포인트로_새로운_객체를_반환한다() {
        // given
        Point point = new Point(1_000);
        // when
        Point newPoint = point.plus(2_000);
        // then
        assertThat(point.getAmount()).isEqualTo(1_000);
        assertThat(newPoint.getAmount()).isEqualTo(3_000);
    }

    /**
     * 포인트를 차감하면 차감된 포인트로 Point 객체를 생성하여 정상적으로 반환하는지 검증
     */
    @Test
    @DisplayName("포인트를 차감하면 차감된 포인트로 새로운 객체를 반환한다.")
    void 포인트를_차감하면_차감된_포인트로_새로운_객체를_반환한다() {
        // given
        Point point = new Point(3_000);
        // when
        Point newPoint = point.minus(2_000);
        // then
        assertThat(point.getAmount()).isEqualTo(3_000);
        assertThat(newPoint.getAmount()).isEqualTo(1_000);
    }

    /**
     * 포인트 합산시 최대한도(1,000,000)를 초과하면 정상적으로 예외를 발생 시키는지 검증
     */
    @Test
    @DisplayName("포인트 합산시 최대한도(1,000,000)를 초과하면 예외를 발생시킨다.")
    void 포인트_합산시_최대한도를_초과하면_예외를_발생시킨다() {
        // given
        Point point = new Point(999_000);
        // when
        var throwableAssert = assertThatThrownBy(() -> point.plus(1_001));
        // then
        throwableAssert.isInstanceOf(IllegalStateException.class);
    }

    /**
     * 포인트 차감 시 잔액이 0미만이면 정상적으로 예외를 발생 시키는지 검증
     */
    @Test
    @DisplayName("포인트 차감시 잔액이 0미만이면 예외를 발생시킨다.")
    void 포인트_차감시_잔액이_0미만이면_예외를_발생시킨다() {
        // given
        Point point = new Point(1_000);
        // when
        var throwableAssert = assertThatThrownBy(() -> point.minus(1_001));
        // then
        throwableAssert.isInstanceOf(IllegalStateException.class);
    }

}