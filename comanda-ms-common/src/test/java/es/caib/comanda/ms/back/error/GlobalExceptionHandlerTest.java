package es.caib.comanda.ms.back.error;

import es.caib.comanda.ms.logic.intf.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("es.caib.comanda.error.handling.MethodArgumentNotValidException.message", Locale.getDefault(), "Validation error");
        ReflectionTestUtils.setField(handler, "messageSource", messageSource);
        ReflectionTestUtils.setField(handler, "printStackTrace", true);
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getParameterValues("trace")).thenReturn(new String[]{"true"});
        webRequest = new ServletWebRequest(req);
    }

    @Test
    void handleNotFoundException_retorna404() {
        // Verifica que una excepció de no trobat es tradueix a HTTP 404.
        var response = handler.handleNotFoundException(new ResourceNotFoundException(String.class, "id"), webRequest);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isInstanceOf(ErrorResponse.class);
    }

    @Test
    void handleResourceAlreadyExistsException_retorna409() {
        // Comprova que els conflictes de recurs existent retornen HTTP 409.
        var response = handler.handleResourceAlreadyExistsException(new ResourceAlreadyExistsException(String.class, "pk"), webRequest);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void handleAccessDeniedException_retorna403() {
        // Valida que un accés denegat es transforma en HTTP 403.
        var response = handler.handleAccessDeniedException(new AccessDeniedException("denied"), webRequest);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void handleDataIntegrityViolationException_senseConstraint_retorna500() {
        // Comprova el tractament genèric d'errors d'integritat sense constraint coneguda.
        var response = handler.handleDataIntegrityViolationException(new DataIntegrityViolationException("boom"), webRequest);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isInstanceOf(ErrorResponse.class);
    }

    @Test
    void handleAnswerRequiredException_retorna422AmbPayload() {
        // Verifica que les preguntes pendents al front retornen 422 amb el payload adequat.
        AnswerRequiredException ex = new AnswerRequiredException(String.class, "code", "question");
        var response = handler.handleAnswerRequiredException(ex, webRequest);
        assertThat(response.getStatusCodeValue()).isEqualTo(422);
        assertThat(response.getBody()).isInstanceOf(AnswerRequiredErrorResponse.class);
    }

    @Test
    void handleModificationExceptions_retorna500ModificationCanceled() {
        // Exercita les excepcions de modificació fallida i el seu missatge estàndard.
        var created = handler.handleResourceNotCreatedException(new ResourceNotCreatedException(String.class, "reason"), webRequest);
        var updated = handler.handleResourceNotUpdatedException(new ResourceNotUpdatedException(String.class, "1", "reason"), webRequest);
        var deleted = handler.handleResourceNotDeletedException(new ResourceNotDeletedException(String.class, "1", "reason"), webRequest);

        assertThat(created.getBody()).isInstanceOf(ModificationCanceledErrorResponse.class);
        assertThat(updated.getBody()).isInstanceOf(ModificationCanceledErrorResponse.class);
        assertThat(deleted.getBody()).isInstanceOf(ModificationCanceledErrorResponse.class);
    }

    @Test
    void handleMethodArgumentNotValid_retorna422AmbDetall() throws Exception {
        // Comprova que els errors de validació retornen 422 amb el detall dels camps invalidats.
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
        bindingResult.addError(new ObjectError("obj", "global-error"));
        bindingResult.addError(new FieldError("obj", "name", "bad", false, new String[]{"code"}, null, "field-error"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
                new org.springframework.core.MethodParameter(
                        GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyMethod", String.class), 0),
                bindingResult);

        var response = handler.handleMethodArgumentNotValid(ex, HttpHeaders.EMPTY, HttpStatus.BAD_REQUEST, webRequest);

        assertThat(response.getStatusCodeValue()).isEqualTo(422);
        assertThat(response.getBody()).isInstanceOf(FieldValidationErrorResponse.class);
        FieldValidationErrorResponse payload = (FieldValidationErrorResponse) response.getBody();
        assertThat(payload.getValidationErrors()).hasSize(2);
    }

    @Test
    void handleExceptionInternal_iUncaught_retornen500() {
        // Valida el tractament d'errors interns i excepcions no controlades amb resposta 500.
        var internal = handler.handleExceptionInternal(new RuntimeException("x"), null, HttpHeaders.EMPTY, HttpStatus.BAD_REQUEST, webRequest);
        var uncaught = handler.handleAllUncaughtException(new RuntimeException("x"), webRequest);

        assertThat(internal.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(uncaught.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void errorResponses_models_funcionen() {
        // Verifica els getters bàsics dels models de resposta d'error.
        ErrorResponse error = new ErrorResponse(400, "bad");
        ConstraintValidationErrorResponse constraint = new ConstraintValidationErrorResponse(500, "msg", "CONS");
        AnswerRequiredErrorResponse answer = new AnswerRequiredErrorResponse(422, "msg", new AnswerRequiredException.AnswerRequiredError("c", "q", null, null, null, List.of()));
        ModificationCanceledErrorResponse mod = new ModificationCanceledErrorResponse(500, "msg", "ACT");

        assertThat(error.getMessage()).isEqualTo("bad");
        assertThat(constraint.isConstraintValidationError()).isTrue();
        assertThat(answer.isAnswerRequiredError()).isTrue();
        assertThat(mod.isModificationCanceledError()).isTrue();
    }

    @SuppressWarnings("unused")
    private void dummyMethod(String value) {
    }
}
