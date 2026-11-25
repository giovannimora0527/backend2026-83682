package com.uniminuto.clinica.catalogo;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.Validar;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Acceso de solo lectura al catálogo de especialidades (la gestión está en el tarifario). */
@Repository
public class EspecializacionRepository {

    /** Acceso JDBC con SQL parametrizado. */
    private final Db db;

    /**
     * @param db acceso a datos.
     */
    public EspecializacionRepository(Db db) {
        this.db = db;
    }

    /** @return especialidades por nombre. */
    public List<EspecializacionDto> listar() {
        return db.lista("SELECT id, nombre, descripcion, codigo_especializacion, activo FROM especializacion ORDER BY nombre")
                .stream().map(f -> new EspecializacionDto(Validar.largo(f, "id"), Validar.cadena(f, "nombre"),
                        Validar.cadena(f, "descripcion"), Validar.cadena(f, "codigo_especializacion"), Validar.bool(f, "activo")))
                .toList();
    }
}
