package kr.hhplus.be.server.external.dataplatform;

import org.springframework.http.HttpStatusCode;

public class DataPlatformApiException extends RuntimeException {

    public DataPlatformApiException(String url, HttpStatusCode httpCode, String message) {
        super("Data Platform API Failed : url = [" + url + "], httpCode = [" + httpCode
            + "], message = [" + message + "]");
    }
}
