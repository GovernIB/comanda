package es.caib.comanda.alarmes.logic.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailHelperTest {

    @Mock
    private JavaMailSender javaMailSender;

    private MailHelper mailHelper;

    @BeforeEach
    void setUp() {
        mailHelper = new MailHelper(Optional.of(javaMailSender));
    }

    @Test
    @DisplayName("Envia correu correctament quan JavaMailSender està present")
    void sendSimple_quanMailSenderPresent_enviamentCorrecte() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        boolean result = mailHelper.sendSimple(
                "from@caib.es", "From Name",
                "to@caib.es", "To Name",
                "Assumpte", "Text del missatge");

        // Assert
        assertThat(result).isTrue();
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("No envia correu quan JavaMailSender no està present")
    void sendSimple_quanMailSenderNoPresent_noEnvia() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        mailHelper = new MailHelper(Optional.empty());

        // Act
        boolean result = mailHelper.sendSimple(
                "from@caib.es", "From Name",
                "to@caib.es", "To Name",
                "Assumpte", "Text del missatge");

        // Assert
        assertThat(result).isFalse();
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }
}
