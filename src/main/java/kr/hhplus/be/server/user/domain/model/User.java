package kr.hhplus.be.server.user.domain.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.hhplus.be.server.point.domain.model.Point;
import kr.hhplus.be.server.user.domain.exception.NotEnoughPointException;

/**
 * 유저 도메인 - 포인트 충전/사용 기능 제공
 */
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount",
        column = @Column(name = "point"))
    })
    private Point point;

    protected User() {}

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
     * 포인트 충전 기능 - 충전 포인트가 0미만일 경우 IllegalStateException 예외 발생 - Point 값 객체의 합산 메서드 plus 호출
     *
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
     * 포인트 사용 기능 - 사용 포인트가 0미만일 경우 IllegalStateException 예외 발생 - Point 값 객체의 차감 메서드 minus 호출
     *
     * @param usePoint 사용 포인트
     */
    public void usePoint(long usePoint) {
        if (usePoint < 0) {
            String msg = String.format("사용 포인트는 0이상 이어야 합니다. 사용 요청 : %d", usePoint);
            throw new IllegalStateException(msg);
        }
        if (this.point.getAmount() < usePoint) {
            String msg = String.format("포인트가 부족 합니다. 현재 포인트 : %d, 사용 요청 : %d",
                this.point.getAmount(), usePoint);
            throw new NotEnoughPointException(msg);
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
