package kr.hhplus.be.server.common.application.lock.queue;

public record QueueTokenInfo(String token, long rank, long totalWait, QueueTokenStatus status) {

}
