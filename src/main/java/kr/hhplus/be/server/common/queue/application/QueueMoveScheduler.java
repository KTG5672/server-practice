package kr.hhplus.be.server.common.queue.application;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class QueueMoveScheduler {

    private final QueueTokenManager queueTokenManager;

    public QueueMoveScheduler(QueueTokenManager queueTokenManager) {
        this.queueTokenManager = queueTokenManager;
    }

    @Scheduled(fixedRate = 1000)
    public void moveQueue() {
        queueTokenManager.moveWaitQueueToActiveQueue();
    }


}
