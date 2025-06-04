package kr.hhplus.be.server.user.domain.model;

import kr.hhplus.be.server.point.domain.model.Point;

/**
 * 유저 도메인
 * - 포인트 충전/사용 기능 제공
 */
public class User {

    private final String id;
    private final String email;
    private final String password;
    private Point point;

    public User(String id, String email, String password, Point point) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.point = point;
    }

    public static User of(String id, String email, String password) {
        return new User(id, email, password, new Point(0));
    }

    /**
     * 포인트 충전 기능
     * - 충전 포인트가 0미만일 경우 IllegalStateException 예외 발생
     * - Point 값 객체의 합산 메서드 plus 호출
     * @param chargePoint 충전 포인트
     */
    public void chargePoint(long chargePoint) {
        if (chargePoint < 0) {
            String msg = String.format("충전 포인트는 0이상 이어야 합니다. 충전 요청 : %d", chargePoint);
            throw new IllegalStateException(msg);
        }
        this.point = point.plus(chargePoint);
    }

    /**
     * 포인트 사용 기능
     * - 사용 포인트가 0미만일 경우 IllegalStateException 예외 발생
     * - Point 값 객체의 차감 메서드 minus 호출
     * @param usePoint 사용 포인트
     */
    public void usePoint(long usePoint) {
        if (usePoint < 0) {
            String msg = String.format("사용 포인트는 0이상 이어야 합니다. 사용 요청 : %d", usePoint);
            throw new IllegalStateException(msg);
        }
        this.point = point.minus(usePoint);
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Point getPoint() {
        return point;
    }
}
