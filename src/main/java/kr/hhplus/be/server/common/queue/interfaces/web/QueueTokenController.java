package kr.hhplus.be.server.common.queue.interfaces.web;

import java.util.Optional;
import kr.hhplus.be.server.common.queue.application.QueueTokenInfo;
import kr.hhplus.be.server.common.queue.application.QueueTokenService;
import kr.hhplus.be.server.common.queue.interfaces.web.dto.QueueTokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 대기열 토큰 발급/조회 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/queue")
public class QueueTokenController {

    private final QueueTokenService queueTokenService;

    public QueueTokenController(QueueTokenService queueTokenService) {
        this.queueTokenService = queueTokenService;
    }

    /**
     * 대기열 토큰 발급 API
     * @param userId 토큰을 발급받을 사용자 ID (PathVariable)
     * @return 발급된 대기열 토큰 정보
    */
    @PostMapping("/token/{userId}")
    public ResponseEntity<QueueTokenResponse> issueToken(@PathVariable("userId") String userId) {
        QueueTokenInfo queueTokenInfo = queueTokenService.enterQueueAndGetToken(userId);
        QueueTokenResponse res = toQueueTokenResponse(queueTokenInfo);
        return ResponseEntity.ok(res);
    }

    /**
     * 대기열 토큰 상태 조회 API
     * @param token 발급 받은 token (PathVariable)
     * @return 대기엽 토큰 정보 (대기열 순서, 예상 대기 시간, 상태)
     */
    @GetMapping("/token/{token}")
    public ResponseEntity<QueueTokenResponse> getTokenInfo(@PathVariable("token") String token) {
        QueueTokenInfo queueTokenInfo = queueTokenService.getToken(token);
        QueueTokenResponse res = toQueueTokenResponse(queueTokenInfo);
        return ResponseEntity.ok(res);
    }

    private QueueTokenResponse toQueueTokenResponse(QueueTokenInfo queueTokenInfo) {
        return QueueTokenResponse.builder()
            .token(queueTokenInfo.token())
            .rank(queueTokenInfo.rank())
            .totalWaitMinute(queueTokenInfo.totalWaitMinute())
            .status(Optional.ofNullable(queueTokenInfo.status()).map(Enum::name).orElse(null))
            .build();
    }

}
