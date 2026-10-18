/**
 * Respuesta paginada estándar que devuelven todos los listados del backend.
 */
export interface PageResponse<T> {
  /** Registros de la página actual. */
  contenido: T[];
  /** Número de página (base 0). */
  pagina: number;
  /** Registros por página. */
  tamano: number;
  /** Total de registros que cumplen el filtro. */
  totalElementos: number;
  /** Total de páginas. */
  totalPaginas: number;
}

/** Dirección de ordenamiento aceptada por el backend. */
export type DireccionOrden = 'asc' | 'desc';

/**
 * Parámetros de paginación y ordenamiento que se envían al backend en cada listado.
 * Los nombres coinciden con los parámetros de la URL esperados por la API.
 */
export interface ParametrosTabla {
  /** Número de página (base 0). */
  pagina: number;
  /** Registros por página. */
  tamano: number;
  /** Nombre lógico de la columna por la que se ordena. */
  orden: string;
  /** Dirección del ordenamiento. */
  direccion: DireccionOrden;
}
