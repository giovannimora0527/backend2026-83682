package com.uniminuto.clinica.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.uniminuto.clinica.entity.ErrorFila;
import com.uniminuto.clinica.entity.enums.AccionCarga;
import com.uniminuto.clinica.entity.enums.EstadoCarga;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** filas no se incluye en el historial. */
public record CargaRs(Long cargaId, String nombreArchivo, String formato, EstadoCarga estado, int totalFilas,
                      int filasValidas, int filasConError, int filasImportadas, String usuario,
                      LocalDateTime fecha, String usuarioConfirmacion, LocalDateTime fechaConfirmacion,
                      @JsonInclude(JsonInclude.Include.NON_NULL) List<Fila> filas) {

    public record Fila(int fila, boolean valida, AccionCarga accion, Map<String, String> datos,
                       List<ErrorFila> errores, List<ErrorFila> advertencias, boolean importada,
                       String errorImportacion) {
    }
}
