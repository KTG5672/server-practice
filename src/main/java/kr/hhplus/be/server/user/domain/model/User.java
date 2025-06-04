package kr.hhplus.be.server.user.domain.model;

import kr.hhplus.be.server.point.domain.model.Point;

/**
 * 유저 도메인
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

    public void chargePoint(long chargePoint) {
        if (chargePoint < 0) {
            String msg = String.format("충전 포인트는 0이상 이어야 합니다. 충전 요청 : %d", chargePoint);
            throw new IllegalStateException(msg);
        }
        this.point = point.plus(chargePoint);
    }

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
