package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.enums.EstadoStock;
import com.uniminuto.clinica.entity.enums.TipoMovimiento;
import com.uniminuto.clinica.entity.enums.TipoProducto;
import com.uniminuto.clinica.models.AjusteRq;
import com.uniminuto.clinica.models.AlertasRs;
import com.uniminuto.clinica.models.DisponibilidadRs;
import com.uniminuto.clinica.models.LoteRq;
import com.uniminuto.clinica.models.LoteRs;
import com.uniminuto.clinica.models.MovimientoRs;
import com.uniminuto.clinica.models.ProductoRq;
import com.uniminuto.clinica.models.ProductoRs;
import java.time.LocalDate;
import java.util.List;

public interface InventarioService {

    List<ProductoRs> listar(String texto, TipoProducto tipo, EstadoStock estadoStock, boolean soloActivos);

    ProductoRs obtener(Long id);

    ProductoRs crear(ProductoRq rq);

    ProductoRs actualizar(Long id, ProductoRq rq);

    ProductoRs cambiarEstado(Long id, boolean activo);

    ProductoRs ingresarLote(Long id, LoteRq rq);

    List<LoteRs> lotes(Long id, boolean soloVigentes);

    ProductoRs ajustar(Long id, AjusteRq rq);

    List<MovimientoRs> movimientos(Long medicamentoId, TipoMovimiento tipo, LocalDate desde, LocalDate hasta);

    AlertasRs alertas();

    DisponibilidadRs disponibilidad(Long medicamentoId);
}
