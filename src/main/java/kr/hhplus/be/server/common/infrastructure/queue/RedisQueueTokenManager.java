package kr.hhplus.be.server.common.infrastructure.queue;

import kr.hhplus.be.server.common.application.lock.queue.QueueTokenInfo;
import kr.hhplus.be.server.common.application.lock.queue.QueueTokenManager;
import org.springframework.stereotype.Component;

@Component
public class RedisQueueTokenManager implements QueueTokenManager {

    @Override
    public QueueTokenInfo issueToken(String userId) {
        return null;
    }

    @Override
    public QueueTokenInfo getTokenInfo(String token) {
        return null;
    }

    @Override
    public void leaveQueue(String token) {

    }
}
