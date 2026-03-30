package es.caib.comanda.salut.back.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RestTemplateLoggingInterceptorTest {

    @Test
    void intercept_quanHiHaBody_delegaLexecucioIRetornaLaResposta() throws IOException {
        RestTemplateLoggingInterceptor interceptor = new RestTemplateLoggingInterceptor();
        HttpRequest request = sampleRequest();
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        byte[] body = "{\"ok\":true}".getBytes();
        when(execution.execute(request, body)).thenReturn(response);

        ClientHttpResponse result = interceptor.intercept(request, body, execution);

        assertThat(result).isSameAs(response);
        verify(execution).execute(request, body);
    }

    @Test
    void intercept_quanNoHiHaBody_tambeDelegaLexecucio() throws IOException {
        RestTemplateLoggingInterceptor interceptor = new RestTemplateLoggingInterceptor();
        HttpRequest request = sampleRequest();
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        byte[] body = new byte[0];
        when(execution.execute(request, body)).thenReturn(response);

        ClientHttpResponse result = interceptor.intercept(request, body, execution);

        assertThat(result).isSameAs(response);
        verify(execution).execute(request, body);
    }

    private static HttpRequest sampleRequest() {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(request.getMethod()).thenReturn(HttpMethod.POST);
        when(request.getURI()).thenReturn(URI.create("http://localhost/test"));
        return request;
    }
}
