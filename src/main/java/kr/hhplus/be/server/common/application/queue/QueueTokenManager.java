package kr.hhplus.be.server.common.application.queue;

public interface QueueTokenManager {

    QueueTokenInfo issueToken(String userId);
    QueueTokenInfo getTokenInfo(String token);
    void leaveQueue(String token);

}
