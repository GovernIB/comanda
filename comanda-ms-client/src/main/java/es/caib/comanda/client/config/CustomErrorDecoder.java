package es.caib.comanda.client.config;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;

import java.util.Date;

public class CustomErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        Exception exception = defaultErrorDecoder.decode(methodKey, response);

        // Només consideram retryable les excepcions que REALMENT són de connectivitat (timeouts, etc.)
        // No convertim qualsevol 5xx en RetryableException per evitar reintents innecessaris i bloquejos.
        if (exception instanceof RetryableException) {
            return new RetryableException(
                    response.status(),
                    exception.getMessage(),
                    response.request().httpMethod(),
                    new Date(),
                    response.request());
        }

        // Per a 5xx retornam l'excepció per defecte de Feign (FeignException),
        // que no és retryable, i permet un maneig més ràpid al codi que crida.
        return exception;
    }
}
