package zw.ac.uz.dpdms.alert.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import zw.ac.uz.dpdms.alert.config.EmailProperties;
import zw.ac.uz.dpdms.alert.entity.DeliveryStatus;

import java.util.List;

/**
 * Sends alerts by email over SMTP (JavaMail, as the brief suggests).
 * Works with Gmail using an app password:
 *   MAIL_USERNAME=you@gmail.com  MAIL_PASSWORD=<16-char app password>
 *   EMAIL_ENABLED=true           EMAIL_RECIPIENTS=a@x.com,b@y.com
 */
@Component
public class EmailNotifier implements AlertNotifier {

    private static final Logger log = LoggerFactory.getLogger(EmailNotifier.class);

    private final EmailProperties props;
    private final ObjectProvider<JavaMailSender> mailSender;

    public EmailNotifier(EmailProperties props, ObjectProvider<JavaMailSender> mailSender) {
        this.props = props;
        this.mailSender = mailSender;
        if (props.enabled() && (isBlank(props.from()) || props.recipientsOrEmpty().isEmpty())) {
            log.warn("Email alerts are enabled but MAIL_USERNAME or EMAIL_RECIPIENTS is not set - every send will fail.");
        }
    }

    @Override
    public String channelName() {
        return "EMAIL";
    }

    @Override
    public boolean isEnabled() {
        return props.enabled();
    }

    @Override
    public List<String> recipients() {
        return props.recipientsOrEmpty();
    }

    @Override
    public String displayRecipient(String recipient) {
        return recipient;
    }

    @Override
    public DeliveryResult send(String recipient, String subject, String messageText) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            return new DeliveryResult(DeliveryStatus.FAILED, "No mail sender configured (check MAIL_HOST)");
        }
        if (isBlank(props.from())) {
            return new DeliveryResult(DeliveryStatus.FAILED, "MAIL_USERNAME is not set");
        }
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(props.from());
            mail.setTo(recipient);
            mail.setSubject(subject);
            mail.setText(messageText
                    + "\n\n--\nSent automatically by the Rushinga DPDMS alert-service. Do not reply.");
            sender.send(mail);
            log.info("Email alert sent to {}", recipient);
            return new DeliveryResult(DeliveryStatus.SENT, "Accepted by SMTP server");
        } catch (Exception e) {
            // Wrong app password, no internet, invalid address, ...
            log.error("Email alert to {} failed: {}", recipient, e.getMessage());
            return new DeliveryResult(DeliveryStatus.FAILED, truncate(String.valueOf(e.getMessage()), 1000));
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }
}
