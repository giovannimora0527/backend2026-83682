package com.uniminuto.clinica.usuario;

import com.uniminuto.clinica.auditoria.AuditoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Controlador REST de usuarios: implementa {@link UsuarioApi} y delega en {@link UsuarioService} y la auditoría. */
@RestController
public class UsuarioApiController implements UsuarioApi {

    /** Reglas de usuarios, roles y permisos. */
    private final UsuarioService servicio;
    /** Consulta del log de auditoría. */
    private final AuditoriaService auditoria;

    /**
     * @param servicio servicio de usuarios.
     * @param auditoria servicio de auditoría.
     */
    public UsuarioApiController(UsuarioService servicio, AuditoriaService auditoria) {
        this.servicio = servicio;
        this.auditoria = auditoria;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<UsuarioDto>> listar() {
        return ResponseEntity.ok(servicio.listar());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> perfiles() {
        return ResponseEntity.ok(servicio.perfiles());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<UsuarioDto> guardar(UsuarioRq datos) {
        return ResponseEntity.ok(servicio.crear(datos));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<UsuarioDto> actualizar(UsuarioRq datos) {
        return ResponseEntity.ok(servicio.actualizar(datos));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> permisos() {
        return ResponseEntity.ok(servicio.matrizPermisos());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> asignarPermisos(long perfilId, PermisosRq datos) {
        return ResponseEntity.ok(servicio.asignarPermisos(perfilId, datos));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> auditoria(String desde, String hasta, String accion, String usuario,
                                                         Integer pagina) {
        return ResponseEntity.ok(auditoria.consultar(desde, hasta, accion, usuario, pagina));
    }
}
