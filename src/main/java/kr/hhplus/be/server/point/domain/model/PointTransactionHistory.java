package kr.hhplus.be.server.point.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "point_transaction_histories")
public class PointTransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "amount", nullable = false)
    private long amount;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    protected PointTransactionHistory() {}

    public PointTransactionHistory(Long id, String userId, long amount, TransactionType type) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
    }

    public static PointTransactionHistory of(String userId, long amount, TransactionType type) {
        return new PointTransactionHistory(null, userId, amount, type);
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public long getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }
}
