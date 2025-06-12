package kr.hhplus.be.server.common.application.queue;

import org.springframework.stereotype.Service;

/**
 * 대기열 토큰 서비스
 * - 대기열 추가 및 토큰 발행 기능
 * - 대기열 정보 조회 기능
 * - 대기열 나가기 기능
 */
@Service
public class QueueTokenService {

    private final QueueTokenManager queueTokenManager;

    public QueueTokenService(QueueTokenManager queueTokenManager) {
        this.queueTokenManager = queueTokenManager;
    }

    /**
     * 대기열 추가 및 토큰 발급
     * @param userId 유저 식별자
     * @return QueueTokenInfo 대기열 토큰 정보
     */
    public QueueTokenInfo enterQueueAndGetToken(String userId) {
        return queueTokenManager.issueToken(userId);
    }

    /**
     * 대기열 정보 조회
     * @param token 대기열 토큰 값
     * @return QueueTokenInfo 대기열 토큰 정보
     */
    public QueueTokenInfo getToken(String token) {
        return queueTokenManager.getTokenInfo(token);
    }

    /**
     * 대기열 나가기
     * @param token 대기열 토큰 값
     */
    public void leaveQueueByToken(String token) {
        queueTokenManager.leaveQueue(token);
    }
}
