package com.uniminuto.clinica.service;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.TestRs;
import org.springframework.web.multipart.MultipartFile;

public interface EmailService {

    TestRs testService();

    void enviarCorreoSimple(String to, String subject, String body)
            throws BadRequestException;

    void enviarCorreo(String to, String subject, String body, String from)
            throws BadRequestException;

    /**
     * Obtiene el remitente configurado para el correo.
     * @return Remitente.
     */
    String getTo();

    void enviarCorreoConAdjuntos(String to, String subject, String body, String from,
                                 MultipartFile adjunto, String filename)
            throws BadRequestException;
}
