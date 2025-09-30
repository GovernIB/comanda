package es.caib.comanda.alarmes.logic.helper;

import es.caib.comanda.alarmes.persist.entity.AlarmaEntity;
import es.caib.comanda.alarmes.persist.repository.AlarmaRepository;
import es.caib.comanda.base.config.BaseConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Enviament de correus d'alarmes.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MailHelper {

	@Autowired(required = false)
	private final JavaMailSender mailSender;

	public void sendSimple(
			String fromAdress,
			String fromName,
			String toAdress,
			String toName,
			String subject,
			String text) throws MessagingException, UnsupportedEncodingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
		helper.setFrom(new InternetAddress(fromAdress, fromName));
		helper.setTo(new InternetAddress(toAdress, toName));
		helper.setSubject(subject);
		helper.setText(text, false);
		mailSender.send(message);
	}

}
