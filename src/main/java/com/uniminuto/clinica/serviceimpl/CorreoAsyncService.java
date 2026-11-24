package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.exception.CorreoException;
import com.uniminuto.clinica.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/** Envio de correos no criticos: no bloquea la peticion y solo registra los fallos. El OTP no pasa por aqui. */
@Service
public class CorreoAsyncService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CorreoAsyncService.class);

    private final EmailService emailService;

    public CorreoAsyncService(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    public void enviar(String to, String subject, String body, String from) {
        try {
            emailService.enviarCorreo(to, subject, body, from);
        } catch (CorreoException ex) {
            LOGGER.error("No se pudo enviar el correo '{}'", subject, ex);
        }
    }
}
