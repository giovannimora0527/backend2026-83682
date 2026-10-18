package co.edu.uniminuto.veterinaria.dto;

/**
 * Datos de un veterinario expuestos por la API.
 *
 * @param id                   identificador
 * @param nombreCompleto       nombres y apellidos
 * @param registroProfesional  registro profesional
 * @param especializacion      nombre de la especialización
 */
public record VeterinarioResponse(Long id, String nombreCompleto, String registroProfesional,
                                  String especializacion) {
}
