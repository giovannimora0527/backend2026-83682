import { HttpErrorResponse } from '@angular/common/http';
import { ApiError } from '../models/api-error.model';

/**
 * Traduce cualquier error de una petición HTTP en un mensaje comprensible para el usuario,
 * sin exponer información técnica (trazas, códigos internos, URLs).
 *
 * - Sin respuesta del servidor (estado 0): error de comunicación con el backend.
 * - Con respuesta: se usa el mensaje que envía el backend (y el detalle por campo, si existe).
 * - Sin mensaje del backend: se usa un texto genérico según el código HTTP.
 *
 * @param error error recibido en el subscribe de una petición
 * @returns mensaje listo para mostrar
 */
export function mensajeDeError(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return 'Ocurrió un error inesperado. Intente de nuevo.';
  }
  if (error.status === 0) {
    return 'No fue posible comunicarse con el servidor. Verifique que el backend esté en ejecución e intente de nuevo.';
  }

  const cuerpo = error.error as Partial<ApiError> | null;
  if (cuerpo && typeof cuerpo.mensaje === 'string') {
    const campos = cuerpo.camposInvalidos ? Object.values(cuerpo.camposInvalidos) : [];
    return campos.length > 0 ? `${cuerpo.mensaje} ${campos.join(' ')}` : cuerpo.mensaje;
  }

  switch (error.status) {
    case 400:
      return 'Los datos enviados no son válidos.';
    case 404:
      return 'El registro solicitado no fue encontrado.';
    case 409:
      return 'La operación entra en conflicto con información existente.';
    case 422:
      return 'La información enviada no cumple las reglas del negocio.';
    default:
      return error.status >= 500
        ? 'Ocurrió un error en el servidor. Intente de nuevo más tarde.'
        : 'No fue posible completar la operación.';
  }
}
