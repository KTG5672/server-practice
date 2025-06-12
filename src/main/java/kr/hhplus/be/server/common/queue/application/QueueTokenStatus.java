package kr.hhplus.be.server.common.queue.application;

/**
 * 대기열 토큰 상태 Enum
 * - ACTIVE (활성화)
 * - WAITING (대기)
 * - EXPIRED (만료)
 */
public enum QueueTokenStatus {
    ACTIVE, WAITING, EXPIRED
}
