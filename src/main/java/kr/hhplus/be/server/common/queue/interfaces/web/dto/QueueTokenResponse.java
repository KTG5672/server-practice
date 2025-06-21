package kr.hhplus.be.server.common.queue.interfaces.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class QueueTokenResponse {

    private String token;
    private Long rank;
    private Long totalWaitMinute;
    private String status;

    @Builder
    public QueueTokenResponse(String token, Long rank, Long totalWaitMinute, String status) {
        this.token = token;
        this.rank = rank;
        this.totalWaitMinute = totalWaitMinute;
        this.status = status;
    }
}
