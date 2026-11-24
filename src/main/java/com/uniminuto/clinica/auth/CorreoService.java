package com.uniminuto.clinica.auth;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Envío de correos con el código de verificación.
 *
 * <p>En Docker Compose los correos llegan a Mailpit (bandeja de prueba en http://localhost:8025).
 * Para usar un servidor real (por ejemplo Gmail) basta con definir MAIL_HOST, MAIL_PORT, MAIL_USER,
 * MAIL_PASSWORD, MAIL_AUTH=true y MAIL_STARTTLS=true. Si no hay servidor configurado (desarrollo local),
 * el código se escribe en el log del backend para poder continuar la prueba.</p>
 */
@Service
public class CorreoService {

    private static final Logger LOG = LoggerFactory.getLogger(CorreoService.class);

    private final ObjectProvider<JavaMailSender> correo;
    private final String host;
    private final String remitente;

    public CorreoService(ObjectProvider<JavaMailSender> correo, @Value("${spring.mail.host:}") String host,
                         @Value("${clinica.correo.remitente}") String remitente) {
        this.correo = correo;
        this.host = host;
        this.remitente = remitente;
    }

    /** @return true si el correo se entregó al servidor SMTP. */
    public boolean enviarCodigo(String destino, String usuario, String codigo, String proposito, int minutos) {
        boolean recuperacion = "RECUPERACION".equals(proposito);
        String asunto = recuperacion ? "Recupera tu contraseña · Clínica Veterinaria Uniminuto"
                : "Tu código de verificación · Clínica Veterinaria Uniminuto";
        JavaMailSender emisor = correo.getIfAvailable();
        if (host == null || host.isBlank() || emisor == null) {
            LOG.warn("Correo no configurado (MAIL_HOST). Código {} para {} <{}>: {}", proposito, usuario, destino, codigo);
            return false;
        }
        try {
            MimeMessage mensaje = emisor.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, false, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(destino);
            helper.setSubject(asunto);
            helper.setText(html(usuario, codigo, recuperacion, minutos), true);
            emisor.send(mensaje);
            return true;
        } catch (MessagingException | MailException e) {
            LOG.error("No fue posible enviar el correo a {}: {}", destino, e.getMessage());
            throw new com.uniminuto.clinica.exception.ClinicaException(
                    org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                    "No fue posible enviar el código por correo. Intenta nuevamente en unos minutos.");
        }
    }

    private static String html(String usuario, String codigo, boolean recuperacion, int minutos) {
        String accion = recuperacion ? "restablecer tu contraseña" : "completar tu inicio de sesión";
        return """
                <div style="font-family:Segoe UI,Arial,sans-serif;max-width:480px;margin:auto;border:1px solid #e3e9f2;border-radius:10px;overflow:hidden">
                  <div style="background:#1450a8;color:#fff;padding:18px 24px;font-size:18px;font-weight:600">Clínica Veterinaria Uniminuto</div>
                  <div style="padding:24px;color:#17324d">
                    <p>Hola <b>%s</b>,</p>
                    <p>Usa este código de 6 caracteres para %s:</p>
                    <p style="font-size:30px;letter-spacing:10px;font-weight:700;text-align:center;background:#f2f6fd;border-radius:8px;padding:14px">%s</p>
                    <p style="color:#647789;font-size:13px">El código vence en %d minutos. Si no fuiste tú, ignora este mensaje y cambia tu contraseña.</p>
                  </div>
                </div>
                """.formatted(org.springframework.web.util.HtmlUtils.htmlEscape(usuario), accion, codigo, minutos);
    }
}
