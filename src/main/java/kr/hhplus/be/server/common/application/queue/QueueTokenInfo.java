package kr.hhplus.be.server.common.application.queue;

public record QueueTokenInfo(String token, Long rank, Long totalWaitMinute, QueueTokenStatus status) {

}
