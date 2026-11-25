package com.uniminuto.clinica.auditoria;

import java.time.LocalDateTime;

/**
 * DTO de un registro del log central de operaciones sensibles.
 *
 * @param usuarioId usuario que realizó la operación (null si no hay sesión, p. ej. login fallido).
 * @param username nombre de usuario.
 * @param perfil rol del usuario.
 * @param accion código de la acción (LOGIN, FACTURA_ANULADA, DESPACHO_REVERTIDO…).
 * @param entidad entidad afectada (factura, despacho, usuario…).
 * @param entidadId identificador de la entidad afectada.
 * @param detalle descripción legible de la operación.
 * @param ip dirección IP de origen.
 * @param fecha fecha y hora (Bogotá).
 */
public record RegistroAuditoria(Long usuarioId, String username, String perfil, String accion, String entidad,
                                Long entidadId, String detalle, String ip, LocalDateTime fecha) {
}
