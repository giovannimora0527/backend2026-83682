import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { ahoraIso, combinarFechaHora } from './fechas';

/**
 * Lee los controles "fecha" y "hora" de un grupo y devuelve la fecha-hora combinada (sin segundos).
 *
 * @param grupo grupo de formulario que contiene los controles fecha y hora
 * @returns fecha-hora yyyy-MM-ddTHH:mm o null si falta alguno de los dos valores
 */
function leerFechaHora(grupo: AbstractControl): string | null {
  const fecha = grupo.get('fecha')?.value as string | null;
  const hora = grupo.get('hora')?.value as string | null;
  if (!fecha || !hora) {
    return null;
  }
  return combinarFechaHora(fecha, hora).slice(0, 16);
}

/**
 * Validador de grupo: la fecha y hora no pueden ser anteriores al momento actual
 * (regla de negocio 6 de las citas). En una edición, si el usuario no cambió la fecha ni la hora,
 * no se valida, para poder modificar otros datos de una cita cuya hora ya pasó.
 *
 * @param original función que devuelve la fecha-hora original de la cita (o null al crear)
 * @returns validador que marca el error {fechaPasada: true}
 */
export function fechaHoraNoPasada(original: () => string | null): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const actual = leerFechaHora(grupo);
    if (actual === null || actual === original()?.slice(0, 16)) {
      return null;
    }
    return actual < ahoraIso() ? { fechaPasada: true } : null;
  };
}

/**
 * Validador de grupo: la fecha y hora no pueden ser futuras (una consulta ya realizada).
 *
 * @returns validador que marca el error {fechaFutura: true}
 */
export function fechaHoraNoFutura(): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const actual = leerFechaHora(grupo);
    if (actual === null) {
      return null;
    }
    return actual > ahoraIso() ? { fechaFutura: true } : null;
  };
}
