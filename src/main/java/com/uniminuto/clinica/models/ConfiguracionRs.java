package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.ConfiguracionClinica;
import java.math.BigDecimal;

public record ConfiguracionRs(String nombre, String nit, String direccion, String telefono, String correo,
                              String prefijoFactura, String prefijoFormula, String prefijoDespacho,
                              BigDecimal topeDescuentoSecretaria) {

    public static ConfiguracionRs de(ConfiguracionClinica c) {
        return new ConfiguracionRs(c.getNombre(), c.getNit(), c.getDireccion(), c.getTelefono(), c.getCorreo(),
                c.getPrefijoFactura(), c.getPrefijoFormula(), c.getPrefijoDespacho(),
                c.getTopeDescuentoSecretaria());
    }
}
