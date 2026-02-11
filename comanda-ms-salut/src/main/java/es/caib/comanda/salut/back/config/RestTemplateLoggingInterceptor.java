package es.caib.comanda.salut.back.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RestTemplateLoggingInterceptor.class);

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        logRequest(request, body);

        ClientHttpResponse response = execution.execute(request, body);
        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) {
        log.info("➡️ REST request");
        log.info("URI      : {}", request.getURI());
        log.info("Method   : {}", request.getMethod());
        log.info("Headers  : {}", request.getHeaders());

        if (body.length > 0) {
            log.info("Body     : {}", new String(body, StandardCharsets.UTF_8));
        } else {
            log.info("Body     : <empty>");
        }
    }
}