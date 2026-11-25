package com.uniminuto.clinica.tablero;

import com.uniminuto.clinica.seguridad.Perfiles;
import com.uniminuto.clinica.seguridad.Permitido;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * Contrato REST del tablero de pendientes: conecta el flujo entre perfiles mostrando a cada usuario
 * el trabajo que le entregó el paso anterior (recepción → médico → despacho → recepción/facturación).
 */
@Tag(name = "Tablero", description = "Pendientes de trabajo según el perfil del usuario")
@Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION, Perfiles.MEDICO, Perfiles.DESPACHO_MEDICO})
@RequestMapping("/tablero")
public interface TableroApi {

    /**
     * Pendientes del usuario autenticado según su perfil.
     *
     * @return lista de tarjetas {clave, titulo, descripcion, cantidad, url, nivel}.
     */
    @Operation(summary = "Pendientes del usuario según su perfil",
            description = "Recepción: citas, atenciones por facturar y saldos. Médico: citas por atender, atenciones "
                    + "abiertas y fórmulas. Despacho: fórmulas por despachar. Administrador: todo lo anterior e inventario.")
    @GetMapping("/pendientes")
    ResponseEntity<List<Map<String, Object>>> pendientes();
}
