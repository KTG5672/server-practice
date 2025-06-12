package kr.hhplus.be.server.common.queue.application;

public record QueueTokenInfo(String token, Long rank, Long totalWaitMinute, QueueTokenStatus status) {

}
