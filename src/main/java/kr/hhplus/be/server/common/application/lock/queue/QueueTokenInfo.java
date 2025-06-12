package kr.hhplus.be.server.common.application.lock.queue;

public record QueueTokenInfo(String token, Long rank, Long totalWaitMinute, QueueTokenStatus status) {

}
