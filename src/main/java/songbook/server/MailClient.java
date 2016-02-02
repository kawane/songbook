package songbook.server;


import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Created by laurent on 02/02/2016.
 */
public class MailClient {

    private Properties config = new Properties();

    public MailClient(Properties config) {
        this.config = config;
    }

    public void send(String from, String to, String subject, String text) throws MessagingException {
        Session session = Session.getInstance(config,
        new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.getProperty("mail.smtp.user"), config.getProperty("mail.smtp.password"));
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(text);

        Transport.send(message);

    }

    public static void main(String[] args) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.user", "legoff.laurent@gmail.com");
        props.put("mail.smtp.password", "*********");
        new MailClient(props).send("legoff.laurent@gmail.com", "legoff.laurent@gmail.com", "test message", "test content message");

    }
}
