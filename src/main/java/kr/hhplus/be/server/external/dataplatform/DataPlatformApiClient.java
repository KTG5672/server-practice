package kr.hhplus.be.server.external.dataplatform;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 데이터 플랫폼 API 호출을 위한 Client 서비스
 */
@Component
@Slf4j
public class DataPlatformApiClient {

    private final WebClient webClient;

    public DataPlatformApiClient(
        @Value("${external-api.data-platform.base-url}") String baseUrl,
        WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    /**
     * 데이터 전송 메서드
     * - WebClient Post 방식으로 데이터 전송
     * - 에러 코드 반환 시 DataPlatformApiException 발생
     * - 타임아웃 5초 설정
     * @param data 요청 바디에 들어갈 데이터
     * @return Mono<DataPlatformApiResponse> status : 상태, message : 메세지
     * @param <T> 데이터 타입
     */
    public <T> Mono<DataPlatformApiResponse> sendData(T data) {

        DataPlatformApiRequest<T> request = new DataPlatformApiRequest<>(data);
        String uri = "/api/v1/data-platform";

        return webClient.post()
            .uri(uri)
            .bodyValue(request)
            .retrieve()
            .onStatus(HttpStatusCode::isError,
                response -> response.bodyToMono(DataPlatformApiResponse.class)
                    .flatMap(body ->
                        Mono.error(new DataPlatformApiException(uri, response.statusCode(),
                            body.message()))))
            .bodyToMono(DataPlatformApiResponse.class)
            .timeout(Duration.ofSeconds(5))
            .doOnError(error -> log.error("Data Platform API Error", error));
    }

}
