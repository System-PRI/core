package pl.edu.amu.wmi.service.impl;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.exception.NotificationManagementException;
import pl.edu.amu.wmi.model.UserInfoDTO;
import pl.edu.amu.wmi.service.NotificationService;
import pl.edu.amu.wmi.util.EMailTemplate;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private static final String USER = "user";

    private final Configuration configuration;
    private final JavaMailSender javaMailSender;

    public NotificationServiceImpl(Configuration configuration, JavaMailSender javaMailSender) {
        this.configuration = configuration;
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendEmail(UserInfoDTO userInfo, EMailTemplate eMailTemplate) {

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            helper.setSubject(eMailTemplate.getSubject());
            helper.setTo(userInfo.getEmail());
            String emailContent = getEmailContent(eMailTemplate, userInfo);
            helper.setText(emailContent, true);

            // TODO: 11/18/2023 add method to not sent spam (based on feature flag)

            javaMailSender.send(mimeMessage);
        } catch (MessagingException | IOException | TemplateException e) {
            log.error("Error during sending e-mail to user with email: {}", userInfo.getEmail(), e);
            throw new NotificationManagementException("Error during sending e-mail to user with email: " + userInfo.getEmail());
        }
    }

    @Override
    public void sendEmail(List<UserInfoDTO> userInfos, EMailTemplate eMailTemplate) {
        userInfos.forEach(userInfo ->
                sendEmail(userInfo, eMailTemplate)
        );
    }

    String getEmailContent(EMailTemplate eMailTemplate, UserInfoDTO userInfo) throws IOException, TemplateException {
        StringWriter stringWriter = new StringWriter();
        Map<String, Object> model = new HashMap<>();
        String userName = userInfo.getFirstName() + " " + userInfo.getLastName();
        model.put(USER, userName);
        configuration.getTemplate(eMailTemplate.getPath()).process(model, stringWriter);
        return stringWriter.getBuffer().toString();
    }
}
