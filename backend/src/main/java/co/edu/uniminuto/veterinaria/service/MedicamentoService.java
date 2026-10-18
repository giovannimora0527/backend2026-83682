package co.edu.uniminuto.veterinaria.service;

import co.edu.uniminuto.veterinaria.dto.MedicamentoResponse;

import java.util.List;

/**
 * Casos de uso de consulta de medicamentos.
 */
public interface MedicamentoService {

    /**
     * Lista los medicamentos vigentes (no vencidos), disponibles para formular.
     *
     * @return medicamentos vigentes ordenados por nombre
     */
    List<MedicamentoResponse> listarVigentes();
}
