/**
 * Utilidades de fechas. Se trabaja con cadenas ISO locales (yyyy-MM-dd y yyyy-MM-ddTHH:mm)
 * y no con objetos Date para evitar corrimientos por zona horaria al mostrar los datos.
 */

/**
 * Rellena con ceros a la izquierda hasta dos dígitos.
 *
 * @param numero número a rellenar
 * @returns número con dos dígitos
 */
const rellenar = (numero: number): string => String(numero).padStart(2, '0');

/**
 * Devuelve la fecha de hoy (hora local) en formato yyyy-MM-dd.
 *
 * @returns fecha actual
 */
export function hoyIso(): string {
  const ahora = new Date();
  return `${ahora.getFullYear()}-${rellenar(ahora.getMonth() + 1)}-${rellenar(ahora.getDate())}`;
}

/**
 * Devuelve la fecha y hora actuales (hora local) en formato yyyy-MM-ddTHH:mm.
 *
 * @returns fecha y hora actuales sin segundos
 */
export function ahoraIso(): string {
  const ahora = new Date();
  return `${hoyIso()}T${rellenar(ahora.getHours())}:${rellenar(ahora.getMinutes())}`;
}

/**
 * Da formato dd/MM/yyyy a una fecha o fecha-hora ISO.
 *
 * @param iso fecha en formato yyyy-MM-dd (o con hora)
 * @returns fecha en formato dd/MM/yyyy
 */
export function formatearFecha(iso: string): string {
  const [anio, mes, dia] = iso.slice(0, 10).split('-');
  return `${dia}/${mes}/${anio}`;
}

/**
 * Extrae la hora (HH:mm) de una fecha-hora ISO.
 *
 * @param iso fecha-hora en formato yyyy-MM-ddTHH:mm[:ss]
 * @returns hora en formato HH:mm
 */
export function formatearHora(iso: string): string {
  return iso.slice(11, 16);
}

/**
 * Da formato dd/MM/yyyy HH:mm a una fecha-hora ISO.
 *
 * @param iso fecha-hora en formato yyyy-MM-ddTHH:mm[:ss]
 * @returns texto legible con fecha y hora
 */
export function formatearFechaHora(iso: string): string {
  return `${formatearFecha(iso)} ${formatearHora(iso)}`;
}

/**
 * Separa una fecha-hora ISO en los valores que usan los inputs date y time.
 *
 * @param iso fecha-hora en formato yyyy-MM-ddTHH:mm[:ss]
 * @returns objeto con la fecha (yyyy-MM-dd) y la hora (HH:mm)
 */
export function separarFechaHora(iso: string): { fecha: string; hora: string } {
  return { fecha: iso.slice(0, 10), hora: iso.slice(11, 16) };
}

/**
 * Une los valores de los inputs date y time en el formato que espera el backend.
 *
 * @param fecha fecha en formato yyyy-MM-dd
 * @param hora hora en formato HH:mm
 * @returns fecha-hora en formato yyyy-MM-ddTHH:mm:00
 */
export function combinarFechaHora(fecha: string, hora: string): string {
  return `${fecha}T${hora}:00`;
}
