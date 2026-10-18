/**
 * Cuerpo estándar de los errores que devuelve el backend.
 */
export interface ApiError {
  /** Momento del error. */
  fecha: string;
  /** Código HTTP numérico. */
  estado: number;
  /** Descripción corta del código HTTP. */
  error: string;
  /** Mensaje comprensible para el usuario. */
  mensaje: string;
  /** Ruta solicitada. */
  ruta: string;
  /** Errores por campo (solo en validaciones), clave = nombre del campo. */
  camposInvalidos: Record<string, string> | null;
}
