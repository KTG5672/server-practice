package kr.hhplus.be.server.point.domain.model;

public class PointTransactionHistory {

    private Long id;
    private String userId;
    private long amount;
    private TransactionType type;

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
