package bytecode.io.ecommerce.service;


import bytecode.io.ecommerce.model.NotificationEmail;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    void sendEmail(NotificationEmail notificationEmail) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("testboot17@gmail.com");
        message.setTo(notificationEmail.getRecipient());
        message.setText((notificationEmail.getBody()));
        message.setSubject(notificationEmail.getSubject());

        mailSender.send(message);

    }
}
