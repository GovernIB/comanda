package es.caib.comanda.alarmes.logic.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Enviament de correus d'alarmes.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MailHelper {

	private final Optional<JavaMailSender> mailSender;

	public boolean sendSimple(
			String fromAdress,
			String fromName,
			String toAdress,
			String toName,
			String subject,
			String text) throws MessagingException, UnsupportedEncodingException {
		if (mailSender.isPresent()) {
			MimeMessage message = mailSender.get().createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
			helper.setFrom(new InternetAddress(fromAdress, fromName));
			helper.setTo(new InternetAddress(toAdress, toName));
			helper.setSubject(subject);
			helper.setText(text, false);
			mailSender.get().send(message);
			return true;
		} else {
			log.warn(
					"No es pot enviar missatge amb assumpte \"{}\" a \"{}\" perquè l'enviament de correus no està configurat",
					subject,
					toName != null ? toName + "<" + toAdress + ">" : toAdress);
			return false;
		}
	}

}
