package es.caib.comanda.permisos.back.validation;

import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.permisos.back.intf.validation.ValidPermisPrincipal;
import es.caib.comanda.permisos.logic.intf.model.Permis;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ValidPermisPrincipalValidatorTest {

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void isValid_fallaQuanNoHiHaNiUsuariNiGrup() {
        // Comprova que la validació custom falla si no s'informa ni usuari ni grup.
        MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(any(String.class), eq(null), any(Locale.class))).thenReturn("Missatge traduït");
        ValidPermisPrincipalValidator validator = new ValidPermisPrincipalValidator(messageSource);
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class, Mockito.RETURNS_DEEP_STUBS);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setPreferredLocales(List.of(new Locale("ca")));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        validator.initialize(mock(ValidPermisPrincipal.class));
        Permis permis = permisBase();

        boolean isValid = validator.isValid(permis, context);

        assertThat(isValid).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context.buildConstraintViolationWithTemplate("Missatge traduït")).addNode("usuari");
        verify(context.buildConstraintViolationWithTemplate("Missatge traduït")).addNode("grup");
    }

    @Test
    void isValid_acceptaQuanHiHaUsuariInformar() {
        // Verifica que la validació accepta el permís quan s'informa almenys un usuari.
        ValidPermisPrincipalValidator validator = new ValidPermisPrincipalValidator(mock(MessageSource.class));
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class, Mockito.RETURNS_DEEP_STUBS);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        validator.initialize(mock(ValidPermisPrincipal.class));
        Permis permis = permisBase();
        permis.setUsuari("user1");

        boolean isValid = validator.isValid(permis, context);

        assertThat(isValid).isTrue();
    }

    @Test
    void isValid_faFallbackAlMissatgePerDefecteSiNoHiHaTraduccio() {
        // Exercita el ramal de fallback quan el MessageSource no troba la clau de validació.
        MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(any(String.class), eq(null), any(Locale.class))).thenThrow(new NoSuchMessageException("missing"));
        ValidPermisPrincipalValidator validator = new ValidPermisPrincipalValidator(messageSource);
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class, Mockito.RETURNS_DEEP_STUBS);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        validator.initialize(mock(ValidPermisPrincipal.class));
        Permis permis = permisBase();

        boolean isValid = validator.isValid(permis, context);

        assertThat(isValid).isFalse();
        verify(context.buildConstraintViolationWithTemplate("És obligatori emplenar el camp usuari o el camp grup")).addNode("usuari");
    }

    private static Permis permisBase() {
        Permis permis = new Permis();
        permis.setEntornAppId(1L);
        permis.setPermisos(List.of("READ"));
        permis.setObjecte(ResourceReference.toResourceReference(7L));
        return permis;
    }
}
