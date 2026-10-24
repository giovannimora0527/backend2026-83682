package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.EmailConfig;
import com.uniminuto.clinica.models.TestRs;
import com.uniminuto.clinica.service.EmailService;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailServiceImpl.class.getName());

    private final JavaMailSender mailSender;
    private final String remitente;

    /**
     * Configuracion del SMTP.
     */
    @Autowired
    private EmailConfig emailConfig;

    public EmailServiceImpl(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    @Override
    public TestRs testService() {
        // Envío de correo de prueba con detalles claros para testeo
        String destinatario = "giovannimora0527@gmail.com";
        String asunto = "Test Service";
        String fechaFormateada = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String mensaje = "Probando servicio de envío de correo desde el backend.\n\n"
                + "Fecha de prueba: " + fechaFormateada
                + "\nEste correo es generado automáticamente para verificar la funcionalidad del "
                + "servicio de correo electrónico."
                + "\n\nSaludos,\nEquipo de Desarrollo.";
        this.enviarCorreo(destinatario, asunto, mensaje,
                "veterinaria.notificacionesuniminuto@gmail.com");
        TestRs rta = new TestRs();
        rta.setRespuesta("Correo enviado satisfactoriamente a: "
                + destinatario + ", asunto: '"
                + asunto
                + "', mensaje: '"
                + mensaje + "'");
        return rta;
    }

    @Override
    public void enviarCorreoSimple(String to, String subject, String body)
            throws BadRequestException {
        enviarCorreo(to, subject, body, getTo());
    }

    @Override
    public void enviarCorreo(String to, String subject, String body, String from)
            throws BadRequestException {
        validarCorreo(to, subject, from);
        if (body == null) {
            throw new BadRequestException("El cuerpo del correo es obligatorio.");
        }

        try {
            this.sendHtmlEmail(to, subject, body, from);
        } catch (MailException e) {
            System.err.println("Error al enviar el correo: "
                    + e.getMessage());
            throw new BadRequestException("Error. " + e.getMessage());
        } catch (MessagingException ex) {
            Logger.getLogger(EmailServiceImpl
                    .class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Envia un correo HTML.
     *
     * @param to       para Destinatario.
     * @param subject  Asunto.
     * @param htmlBody Cuerpo del correo.
     * @param from     Remitente del correo.
     * @throws MessagingException excepcion.
     */
    @Async
    void sendHtmlEmail(final String to,
                       final String subject,
                       final String htmlBody,
                       final String from)
            throws MessagingException {

        // Configuración de las propiedades del servidor SMTP
        Properties properties = new Properties();
        properties.put("mail.smtp.host", emailConfig.getSmtpHost());
        properties.put("mail.smtp.port", emailConfig.getSmtpPort());
        properties.put("mail.smtp.auth", emailConfig.getSmtpAuth());
        properties.put("mail.smtp.starttls.enable", emailConfig.getStartTls());
        properties.put("mail.smtp.ssl.enable", emailConfig.getSslEnable());

        // Crear la sesión de correo con autenticación
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailConfig.getUsername(),
                        emailConfig.getPassword());
            }
        });

        // Crear el mensaje de correo
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(to));
        message.setSubject(subject);

        // Establecer el cuerpo del mensaje en formato HTML
        message.setContent(htmlBody, "text/html; charset=utf-8");

        // Enviar el correo
        Transport.send(message);
        System.out.println("Correo enviado con éxito a " + to);
    }

    @Override
    public String getTo() {
        return remitente;
    }

    @Override
    public void enviarCorreoConAdjuntos(String to, String subject, String body, String from,
                                        MultipartFile adjunto, String filename)
            throws BadRequestException {
        validarCorreo(to, subject, from);
        if (body == null) {
            throw new BadRequestException("El cuerpo del correo es obligatorio.");
        }
        if (adjunto == null || adjunto.isEmpty()) {
            throw new BadRequestException("El archivo adjunto es obligatorio y no puede estar vacío.");
        }
        if (filename == null || filename.isBlank()) {
            throw new BadRequestException("El nombre del archivo adjunto es obligatorio.");
        }

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to.trim());
            helper.setFrom(from.trim());
            helper.setSubject(subject.trim());
            helper.setText(body, false);
            helper.addAttachment(filename.trim(), adjunto);
            mailSender.send(message);
        } catch (MessagingException | MailException ex) {
            LOGGER.log(Level.SEVERE, "No fue posible enviar el correo con adjunto.", ex);
            throw new BadRequestException("No fue posible enviar el correo con adjunto.");
        }
    }

    private void validarCorreo(String to, String subject, String from) {
        if (to == null || to.isBlank()) {
            throw new BadRequestException("El destinatario del correo es obligatorio.");
        }
        if (subject == null || subject.isBlank()) {
            throw new BadRequestException("El asunto del correo es obligatorio.");
        }
        if (from == null || from.isBlank()) {
            throw new BadRequestException("El remitente del correo es obligatorio.");
        }
    }
}
