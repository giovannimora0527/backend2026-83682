package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.CorreoException;
import com.uniminuto.clinica.models.TestRs;
import com.uniminuto.clinica.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final String remitente;

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
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(to.trim());
            helper.setFrom(from.trim());
            helper.setSubject(subject.trim());
            helper.setText(body, true);
            mailSender.send(message);
        } catch (MessagingException | MailException ex) {
            throw new CorreoException("No fue posible enviar el correo.", ex);
        }
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
            throw new CorreoException("No fue posible enviar el correo con adjunto.", ex);
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
