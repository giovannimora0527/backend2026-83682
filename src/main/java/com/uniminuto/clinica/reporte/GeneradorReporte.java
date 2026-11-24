package com.uniminuto.clinica.reporte;

import com.uniminuto.clinica.security.Rol;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/** Un reporte del catalogo. Se agrega uno nuevo declarando otro bean. */
public interface GeneradorReporte {

    String codigo();

    String titulo();

    String grupo();

    Set<String> rolesPermitidos();

    List<Filtro> filtros();

    Tabla generar(Filtros filtros);

    /** Reporte definido por su consulta; todos los del catalogo actual son del administrador. */
    static GeneradorReporte de(String codigo, String titulo, String grupo, List<Filtro> filtros,
                               Function<Filtros, Tabla> consulta) {
        return new GeneradorReporte() {
            @Override
            public String codigo() {
                return codigo;
            }

            @Override
            public String titulo() {
                return titulo;
            }

            @Override
            public String grupo() {
                return grupo;
            }

            @Override
            public Set<String> rolesPermitidos() {
                return Set.of(Rol.ADMINISTRADOR);
            }

            @Override
            public List<Filtro> filtros() {
                return filtros;
            }

            @Override
            public Tabla generar(Filtros valores) {
                return consulta.apply(valores);
            }
        };
    }
}
