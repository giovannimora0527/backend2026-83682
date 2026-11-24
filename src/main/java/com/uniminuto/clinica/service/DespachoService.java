package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.enums.EstadoDespacho;
import com.uniminuto.clinica.models.Descarga;
import com.uniminuto.clinica.models.DespachoConfirmarRq;
import com.uniminuto.clinica.models.DespachoFormulaRs;
import com.uniminuto.clinica.models.DespachoMedicamentoRs;
import com.uniminuto.clinica.models.DespachoRs;
import com.uniminuto.clinica.models.FormulaHistorialRs;
import com.uniminuto.clinica.models.FormulaPendienteRs;
import java.time.LocalDate;
import java.util.List;

public interface DespachoService {

    List<FormulaPendienteRs> formulasPendientes(String texto, String numero, LocalDate desde, LocalDate hasta,
                                                EstadoDespacho estado);

    DespachoFormulaRs consultar(Long formulaId);

    DespachoFormulaRs preparar(Long formulaId);

    DespachoRs confirmar(Long despachoId, DespachoConfirmarRq rq, String idempotencyKey);

    /** Cierra una preparacion sin entregar nada. */
    DespachoFormulaRs liberar(Long despachoId);

    DespachoFormulaRs cancelar(Long formulaId, String motivo);

    DespachoRs revertir(Long despachoId, String motivo);

    Descarga comprobante(Long despachoId);

    List<DespachoRs> historial(LocalDate desde, LocalDate hasta, String texto, String estado);

    List<DespachoMedicamentoRs> historialMedicamento(Long medicamentoId);

    List<FormulaHistorialRs> historialEstados(Long formulaId);
}
