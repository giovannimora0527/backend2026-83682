
package com.uniminuto.clinica.dto;

/**
 * DTO (Data Transfer Object).
 *
 * Sirve para enviar información de las mascotas
 * desde Spring Boot hacia Angular.
 *
 * Incluye datos de la raza y el propietario.
 *
 * No modifica la base de datos.
 */
public record MascotaDetalleDTO(

        // Identificador de la mascota.
        Integer mascotaId,

        // Nombre de la mascota.
        String nombreMascota,

        // Edad de la mascota.
        Integer edad,

        // Fecha de registro.
        String fechaRegistro,

        // Información de la raza.
        RazaDetalle raza,

        // Información del propietario.
        ClienteDetalle cliente

) {

    /**
     * DTO interno para representar la raza.
     */
    public record RazaDetalle(
            Integer id,
            String nombre,
            String especie
    ) {}

    /**
     * DTO interno para representar el propietario.
     */
    public record ClienteDetalle(
            Long clienteId,
            String nombres,
            String apellidos
    ) {}
}
