package kr.hhplus.be.server.point.domain.model;

/**
 * 포인트 도메인 - 불변 객체로 충전, 사용 메서드 호출 시 새로운 Point 객체 반환
 */
public class Point {

    private final long amount;

    private static final long MAX_AMOUNT = 1_000_000;

    public Point(long amount) {
        if (isNegativeNumber(amount)) {
            String msg = String.format("포인트는 0이상이어야 합니다. 포인트 : %d", amount);
            throw new IllegalStateException(msg);
        }
        this.amount = amount;
    }

    /**
     * 포인트 합산 메서드
     * 포인트를 합산하여 새로운 Point 객체를 생성하여 반환
     * - 결과가 최대한도 초과될 경우 예외 발생
     * @param plusAmount 합산할 포인트
     * @return 새로운 Point 객체
     */
    public Point plus(long plusAmount) {
        long result = this.amount + plusAmount;
        if (MAX_AMOUNT < result) {
            String msg = String.format("포인트는 최대한도 이하이어야 합니다. 최대한도 : %d", MAX_AMOUNT);
            throw new IllegalStateException(msg);
        }
        return new Point(result);
    }

    /**
     * 포인트 차감 메서드
     * 포인트를 차감하여 새로운 Point 객체를 생성하여 반환
     * - 결과가 음수일 경우 생성자에서 예외 발생
     * @param minusPoint 차감할 포인트
     * @return 새로운 Point 객체
     */
    public Point minus(long minusPoint) {
        return new Point(this.amount - minusPoint);
    }

    private boolean isNegativeNumber(long num) {
        return num < 0;
    }

    public Long getAmount() {
        return amount;
    }

}
