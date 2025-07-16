package kr.hhplus.be.server.external.dataplatform;

import reactor.core.publisher.Mono;

public interface DataPlatformClient {
    <T> Mono<?> sendData(T data);
}
