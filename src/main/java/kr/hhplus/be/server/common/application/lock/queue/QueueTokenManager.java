package kr.hhplus.be.server.common.application.lock.queue;

public interface QueueTokenManager {

    QueueTokenInfo issueToken(String userId);
    QueueTokenInfo getTokenInfo(String token);
    void leaveQueue(String token);

}
