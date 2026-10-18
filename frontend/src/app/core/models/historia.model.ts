/** Estados de una historia clínica. */
export type EstadoHistoria = 'ABIERTA' | 'CERRADA';

/** Texto legible de cada estado de historia. */
export const ETIQUETA_ESTADO_HISTORIA: Record<EstadoHistoria, string> = {
  ABIERTA: 'Abierta',
  CERRADA: 'Cerrada',
};

/** Historia clínica (consulta) tal como la devuelve el backend. */
export interface Historia {
  /** Identificador. */
  id: number;
  /** Fecha y hora de la consulta (yyyy-MM-ddTHH:mm:ss). */
  fechaHora: string;
  /** Identificador del cliente. */
  clienteId: number;
  /** Nombre completo del cliente. */
  clienteNombre: string;
  /** Identificador de la mascota. */
  mascotaId: number;
  /** Nombre de la mascota. */
  mascotaNombre: string;
  /** Identificador de la cita que originó la historia. */
  citaId: number;
  /** Identificador del veterinario. */
  veterinarioId: number;
  /** Nombre completo del veterinario. */
  veterinarioNombre: string;
  /** Motivo de consulta. */
  motivo: string;
  /** Diagnóstico. */
  diagnostico: string;
  /** Observaciones adicionales. */
  observaciones: string | null;
  /** Estado de la historia. */
  estado: EstadoHistoria;
}

/** Datos para registrar una historia clínica. */
export interface HistoriaRequest {
  /** Cliente dueño de la mascota. */
  clienteId: number;
  /** Mascota (debe pertenecer al cliente). */
  mascotaId: number;
  /** Cita atendida que origina la historia. */
  citaId: number;
  /** Veterinario que realizó la consulta. */
  veterinarioId: number;
  /** Fecha y hora de la consulta (yyyy-MM-ddTHH:mm:ss). */
  fechaHora: string;
  /** Motivo de consulta. */
  motivo: string;
  /** Diagnóstico. */
  diagnostico: string;
  /** Observaciones (opcional). */
  observaciones: string;
}

/** Criterios de filtrado del listado de historias (todos opcionales). */
export interface HistoriaFiltro {
  /** Texto contenido en el nombre del cliente. */
  cliente?: string;
  /** Texto contenido en el nombre de la mascota. */
  mascota?: string;
  /** Texto contenido en el motivo de consulta. */
  motivo?: string;
  /** Identificador exacto de la mascota. */
  mascotaId?: number | null;
  /** Identificador exacto del veterinario. */
  veterinarioId?: number | null;
  /** Estado de la historia ('' = todos). */
  estado?: EstadoHistoria | '';
  /** Fecha inicial (yyyy-MM-dd). */
  desde?: string;
  /** Fecha final (yyyy-MM-dd). */
  hasta?: string;
}

/** Anotación de seguimiento de una historia clínica. */
export interface Anotacion {
  /** Identificador. */
  id: number;
  /** Identificador de la historia. */
  historiaId: number;
  /** Fecha y hora en que se registró (la asigna el servidor). */
  fechaHora: string;
  /** Identificador del veterinario autor. */
  veterinarioId: number;
  /** Nombre completo del veterinario autor. */
  veterinarioNombre: string;
  /** Descripción de lo realizado. */
  descripcion: string;
  /** Observación del veterinario. */
  observacion: string;
  /** Diagnóstico o evolución del paciente. */
  diagnosticoEvolucion: string;
}

/** Datos para agregar una anotación (la fecha y hora la asigna el servidor). */
export interface AnotacionRequest {
  /** Veterinario que escribe la anotación. */
  veterinarioId: number;
  /** Descripción de lo realizado. */
  descripcion: string;
  /** Observación del veterinario. */
  observacion: string;
  /** Diagnóstico o evolución. */
  diagnosticoEvolucion: string;
}

/** Criterios de filtrado de las anotaciones de una historia (todos opcionales). */
export interface AnotacionFiltro {
  /** Texto contenido en la descripción, observación o diagnóstico/evolución. */
  texto?: string;
  /** Identificador exacto del veterinario. */
  veterinarioId?: number | null;
  /** Fecha inicial (yyyy-MM-dd). */
  desde?: string;
  /** Fecha final (yyyy-MM-dd). */
  hasta?: string;
}
