package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.ConfiguracionRq;
import com.uniminuto.clinica.models.ConfiguracionRs;

public interface ConfiguracionService {

    ConfiguracionRs obtener();

    ConfiguracionRs actualizar(ConfiguracionRq rq);

    /** Siguiente numero de formula, por ejemplo FM-000001. Exige una transaccion. */
    String siguienteFormula();
}
