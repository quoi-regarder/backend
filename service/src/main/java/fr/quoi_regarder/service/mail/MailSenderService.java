package fr.quoi_regarder.service.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailSenderService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * Sends an email asynchronously using a Thymeleaf template
     */
    @Async("emailExecutor")
    public CompletableFuture<Void> sendMessageUsingThymeleafTemplateAsync(
            String to,
            String subject,
            Map<String, Object> templateModel,
            String templateName,
            Locale locale
    ) {
        return CompletableFuture.runAsync(() -> {
            try {
                sendMessageUsingThymeleafTemplate(to, subject, templateModel, templateName, locale);
                log.debug("Email successfully sent to {}", to);
            } catch (Exception e) {
                log.error("Error while sending email to {}: {}", to, e.getMessage());
            }
        });
    }

    private void sendMessageUsingThymeleafTemplate(
            String to,
            String subject,
            Map<String, Object> templateModel,
            String templateName,
            Locale locale
    ) throws MessagingException, UnsupportedEncodingException {
        Context thymeleafContext = new Context(locale);
        thymeleafContext.setVariables(templateModel);

        String html = templateEngine.process(templateName, thymeleafContext);

        sendHtmlMessage(to, subject, html);
    }

    private void sendHtmlMessage(String to, String subject, String html) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom(from, "Quoi Regarder");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(mimeMessage);
    }
}