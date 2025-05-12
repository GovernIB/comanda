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

        // Si és un error 5xx o de connexió, permetem el reintent
        if (response.status() >= 500 || exception instanceof RetryableException) {
            return new RetryableException(
                    response.status(),
                    exception.getMessage(),
                    response.request().httpMethod(),
                    new Date(),
                    response.request());
        }

        return exception;
    }
}
