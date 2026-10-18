package co.edu.uniminuto.veterinaria.dto;

/**
 * Datos de un cliente expuestos por la API.
 *
 * @param id              identificador
 * @param tipoDocumento   tipo de documento
 * @param numeroDocumento número de documento
 * @param nombres         nombres
 * @param apellidos       apellidos
 * @param nombreCompleto  nombres y apellidos
 * @param telefono        teléfono
 * @param direccion       dirección
 * @param activo          si el cliente está activo
 */
public record ClienteResponse(Long id, String tipoDocumento, String numeroDocumento, String nombres,
                              String apellidos, String nombreCompleto, String telefono, String direccion,
                              boolean activo) {
}
