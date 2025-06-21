package kr.hhplus.be.server.common.queue.interfaces.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import kr.hhplus.be.server.common.queue.application.QueueTokenInfo;
import kr.hhplus.be.server.common.queue.application.QueueTokenService;
import kr.hhplus.be.server.common.queue.application.QueueTokenStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(QueueTokenController.class)
class QueueTokenControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    QueueTokenService queueTokenService;

    @Test
    @DisplayName("유저 토큰 발급 API 동작 테스트")
    void 유저_토큰_발급_API_테스트() throws Exception {
        // given
        String userId = "test-user";
        QueueTokenInfo mockToken = new QueueTokenInfo("token-123", 1L, 0L, QueueTokenStatus.ACTIVE);
        when(queueTokenService.enterQueueAndGetToken(userId)).thenReturn(mockToken);

        // when & then
        mockMvc.perform(post("/api/v1/queue/token/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-123"))
                .andExpect(jsonPath("$.rank").value(1))
                .andExpect(jsonPath("$.totalWaitMinute").value(0));
    }

    @Test
    @DisplayName("대기열 상태 조회 API 동작 테스트")
    void 대기열_상태_조회_API_테스트() throws Exception {
        // given
        String token = "token-123";
        QueueTokenInfo mockToken = new QueueTokenInfo("token-123", 100_001L, 2L, QueueTokenStatus.WAITING);
        when(queueTokenService.getToken(token)).thenReturn(mockToken);

        // when & then
        mockMvc.perform(get("/api/v1/queue/token/{token}", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-123"))
                .andExpect(jsonPath("$.rank").value(100_001))
                .andExpect(jsonPath("$.totalWaitMinute").value(2));
    }
}