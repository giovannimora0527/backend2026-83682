import { HttpParams } from '@angular/common/http';

/**
 * Convierte un objeto de criterios en parámetros de URL, omitiendo los valores vacíos
 * (null, undefined o cadena vacía) para no enviar filtros que el usuario no usó.
 *
 * @param criterios objeto con los parámetros (por ejemplo filtro + paginación)
 * @returns parámetros HTTP listos para usar en una petición GET
 */
export function construirParams(criterios: object): HttpParams {
  let params = new HttpParams();
  for (const [clave, valor] of Object.entries(criterios)) {
    if (valor === null || valor === undefined || valor === '') {
      continue;
    }
    params = params.set(clave, String(valor));
  }
  return params;
}
