package kr.hhplus.be.server.common.queue.application;

public interface QueueTokenManager {

    QueueTokenInfo issueToken(String userId);
    QueueTokenInfo getTokenInfo(String token);
    void leaveQueue(String token);
    void moveWaitQueueToActiveQueue();

}
