package es.caib.comanda.ms.exception;

public class ComandaApiException extends RuntimeException{

    public ComandaApiException() {
        super();
    }

    public ComandaApiException(String message) {
        super(message);
    }

    public ComandaApiException(String message, Throwable cause) {
        super(message, cause);
    }

}
