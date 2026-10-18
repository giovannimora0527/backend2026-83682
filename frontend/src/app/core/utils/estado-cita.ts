import { EstadoCita } from '../models/cita.model';

/**
 * Transiciones permitidas entre estados de una cita (espejo de la regla que valida el backend).
 *
 * Justificación: una cita ATENDIDA es información histórica y una CANCELADA no debe comportarse
 * como activa, por eso ambos son estados terminales. Una cancelada solo vuelve a estar activa con la
 * operación explícita "Reactivar".
 */
export const TRANSICIONES_CITA: Record<EstadoCita, EstadoCita[]> = {
  PENDIENTE: ['CONFIRMADA', 'PROGRAMADA', 'CANCELADA'],
  CONFIRMADA: ['PROGRAMADA', 'ATENDIDA', 'CANCELADA'],
  PROGRAMADA: ['CONFIRMADA', 'ATENDIDA', 'CANCELADA'],
  ATENDIDA: [],
  CANCELADA: [],
};

/** Estados con los que se puede registrar una cita nueva. */
export const ESTADOS_INICIALES: EstadoCita[] = ['PENDIENTE', 'CONFIRMADA', 'PROGRAMADA'];

/**
 * Devuelve los estados que se pueden elegir en el formulario.
 *
 * @param actual estado actual de la cita, o null si se está creando
 * @returns estados disponibles (en una edición incluye el estado actual)
 */
export function estadosDisponibles(actual: EstadoCita | null): EstadoCita[] {
  return actual === null ? ESTADOS_INICIALES : [actual, ...TRANSICIONES_CITA[actual]];
}

/**
 * Indica si una cita en el estado dado puede editarse.
 *
 * @param estado estado de la cita
 * @returns true si el estado admite modificaciones
 */
export function citaEditable(estado: EstadoCita): boolean {
  return TRANSICIONES_CITA[estado].length > 0;
}

/**
 * Explica por qué una cita no puede editarse (se muestra como ayuda en el botón deshabilitado).
 *
 * @param estado estado de la cita
 * @returns texto explicativo, o cadena vacía si la cita sí es editable
 */
export function motivoCitaNoEditable(estado: EstadoCita): string {
  if (estado === 'CANCELADA') {
    return 'Una cita cancelada no se modifica. Use «Reactivar» para volver a activarla.';
  }
  if (estado === 'ATENDIDA') {
    return 'Una cita atendida es información histórica y no puede modificarse.';
  }
  return '';
}
