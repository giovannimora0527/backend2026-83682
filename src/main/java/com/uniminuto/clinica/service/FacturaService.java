package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.enums.EstadoFactura;
import com.uniminuto.clinica.models.CitaPendienteFacturaRs;
import com.uniminuto.clinica.models.Descarga;
import com.uniminuto.clinica.models.FacturaGenerarRq;
import com.uniminuto.clinica.models.FacturaHistorialRs;
import com.uniminuto.clinica.models.FacturaPreviaRs;
import com.uniminuto.clinica.models.FacturaRs;
import com.uniminuto.clinica.models.PagoRq;
import com.uniminuto.clinica.models.PagoRs;
import com.uniminuto.clinica.models.RespuestaRs;
import java.time.LocalDate;
import java.util.List;

public interface FacturaService {

    List<CitaPendienteFacturaRs> citasPendientes();

    FacturaPreviaRs previsualizar(Long citaId);

    FacturaRs generar(FacturaGenerarRq rq);

    List<FacturaRs> listar(String numero, EstadoFactura estado, LocalDate desde, LocalDate hasta, Long clienteId,
                           Integer mascotaId);

    List<FacturaRs> listarPorCliente(Long clienteId);

    List<FacturaRs> listarPorMascota(Integer mascotaId);

    FacturaRs obtener(Long id);

    Descarga pdf(Long id);

    FacturaRs pagar(Long id, PagoRq rq);

    List<PagoRs> pagos(Long id);

    RespuestaRs<FacturaRs> anular(Long id, String motivo);

    List<FacturaHistorialRs> historialEstados(Long id);
}
