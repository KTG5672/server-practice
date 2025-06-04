package kr.hhplus.be.server.point.domain.repository;

import kr.hhplus.be.server.point.domain.model.PointTransactionHistory;

public interface PointTransactionHistoryRepository {

    PointTransactionHistory save(PointTransactionHistory pointTransactionHistory);

}
