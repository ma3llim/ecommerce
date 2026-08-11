package org.ecommerce.common.notification.provider.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GmailEmailProvider implements EmailProvider {
    private final JavaMailSender mailSender;

    @Override
    public void send(String recipient, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email provider send operation completed successfully: recipient={}", recipient);
        } catch (MessagingException exception) {
            log.error("Email provider failed while preparing email message", exception);
            throw new IllegalStateException("Failed to send email notification", exception);
        } catch (Exception exception) {
            log.error("Email provider failed while sending email", exception);
            throw new IllegalStateException("Failed to send email notification", exception);
        }
    }
}
