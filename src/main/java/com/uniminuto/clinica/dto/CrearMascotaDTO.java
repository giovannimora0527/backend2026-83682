
package com.uniminuto.clinica.dto;

/**
 * DTO PARA CREAR MASCOTAS
 *
 * Recibe los datos enviados desde Angular.
 *
 * No solicitamos mascotaId porque MySQL
 * lo genera automáticamente.
 *
 * La fecha de registro será asignada
 * por Spring Boot.
 */
public record CrearMascotaDTO(

        // Nombre de la nueva mascota.
        String nombreMascota,

        // Edad en años.
        Integer edad,

        // Identificador de la raza.
        Integer razaId,

        // Identificador del propietario.
        Long clienteId

) {
}
