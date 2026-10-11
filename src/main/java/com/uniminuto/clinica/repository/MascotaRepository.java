package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio encargado de acceder a los registros
 * de mascotas almacenados en MySQL.
 */
@Repository
public interface MascotaRepository
        extends JpaRepository<Mascota, Integer> {

    /**
     * Consulta los datos de mascotas, razas y propietarios.
     *
     * nativeQuery = true:
     * Utiliza directamente los nombres de tablas
     * y columnas existentes en MySQL.
     *
     * No modifica ningún registro.
     */
    @Query(value = """
    SELECT
        m.mascota_id,
        m.nombre_mascota,
        m.edad,
        m.fecha_registro,
        r.raza_id,
        r.nombre,
        r.especie,
        c.cliente_id,
        c.nombres,
        c.apellidos
    FROM mascota m
    LEFT JOIN raza r
        ON m.raza_id = r.raza_id
    LEFT JOIN cliente c
        ON m.cliente_id = c.cliente_id
    ORDER BY m.mascota_id
    """, nativeQuery = true)
    List<Object[]> listarMascotasConDetalle();


    /*
     * JpaRepository proporciona métodos como:
     *
     * findAll()       -> Consultar todas las mascotas.
     * findById(id)    -> Buscar una mascota por su ID.
     * save(mascota)   -> Crear o actualizar una mascota.
     * deleteById(id)  -> Eliminar una mascota.
     *
     * Por ahora no necesitamos agregar métodos.
     */


    /**
     * Comprueba si existe una raza en MySQL.
     *
     * @param razaId identificador de la raza.
     * @return true cuando existe la raza.
     */
    @Query(
            value = """
        SELECT COUNT(*)
        FROM raza
        WHERE raza_id = :razaId
        """,
            nativeQuery = true
    )
    long contarRazaPorId(@Param("razaId") Integer razaId);

}