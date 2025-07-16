package kr.hhplus.be.server.external.dataplatform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import kr.hhplus.be.server.external.WebClientConfig;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@SuppressWarnings("unchecked")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = WebClientConfig.class)
@ActiveProfiles("test")
class DataPlatformApiClientTest {

    @Autowired
    WebClient.Builder webClientBuilder;

    MockWebServer mockWebServer;

    @BeforeEach
    void setUp() {
        mockWebServer = new MockWebServer();
        try {
            mockWebServer.start();
        } catch (IOException e) {
            fail("MockWebServer failed to start");
        }
    }


    @Test
    @DisplayName("데이터플랫폼 Mock API 호출 성공 테스트")
    void Mock_데이터플랫폼_API_호출_성공_테스트() {

        // given
        mockWebServer.enqueue(new MockResponse()
            .setBody("{\"status\":\"OK\"}")
            .addHeader("Content-Type", "application/json"));
        String url = mockWebServer.url("/").toString();

        DataPlatformApiClient dataPlatformApiClient = new DataPlatformApiClient(url,
            webClientBuilder);

        // when
        Mono<DataPlatformApiResponse> response = (Mono<DataPlatformApiResponse>) dataPlatformApiClient.sendData("String value");

        // then
        DataPlatformApiResponse result = response.block();
        assertThat(result).isNotNull();

    }


    @Test
    @DisplayName("데이터플랫폼 Mock API 호출 실패 테스트")
    void Mock_데이터플랫폼_API_호출_실패_테스트() {

        // given
        mockWebServer.enqueue(new MockResponse()
            .setBody(
                "{\"status\":\"FAIL\","
                + "\"message\":\"Not Valid Parameter\"}")
            .setResponseCode(400)
            .addHeader("Content-Type", "application/json"));
        String url = mockWebServer.url("/").toString();

        DataPlatformApiClient dataPlatformApiClient = new DataPlatformApiClient(url,
            webClientBuilder);

        // when
        Mono<DataPlatformApiResponse> response = (Mono<DataPlatformApiResponse>) dataPlatformApiClient.sendData("String value");
        var throwableAssert = assertThatThrownBy(response::block);

        // then
        throwableAssert.isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Not Valid Parameter");

    }

    @AfterEach
    void afterEach() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }
}