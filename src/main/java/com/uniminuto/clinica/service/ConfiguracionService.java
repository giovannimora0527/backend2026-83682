package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.ConfiguracionRq;
import com.uniminuto.clinica.models.ConfiguracionRs;

public interface ConfiguracionService {

    ConfiguracionRs obtener();

    ConfiguracionRs actualizar(ConfiguracionRq rq);

    /** Siguiente numero de formula, por ejemplo FM-000001. Exige una transaccion. */
    String siguienteFormula();

    /** Siguiente numero de despacho, por ejemplo DP-000001. Exige una transaccion. */
    String siguienteDespacho();

    /** Siguiente numero de factura, por ejemplo FV-000001. Exige una transaccion. */
    String siguienteFactura();
}
