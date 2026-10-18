/** Estados posibles de una cita (los mismos que valida el backend). */
export type EstadoCita = 'PENDIENTE' | 'CONFIRMADA' | 'ATENDIDA' | 'CANCELADA' | 'PROGRAMADA';

/** Texto legible de cada estado de cita. */
export const ETIQUETA_ESTADO_CITA: Record<EstadoCita, string> = {
  PENDIENTE: 'Pendiente',
  CONFIRMADA: 'Confirmada',
  ATENDIDA: 'Atendida',
  CANCELADA: 'Cancelada',
  PROGRAMADA: 'Programada',
};

/** Todos los estados, en el orden en que se muestran en los filtros. */
export const ESTADOS_CITA: EstadoCita[] = ['PENDIENTE', 'CONFIRMADA', 'PROGRAMADA', 'ATENDIDA', 'CANCELADA'];

/** Cita médica tal como la devuelve el backend. */
export interface Cita {
  /** Identificador. */
  id: number;
  /** Fecha y hora (yyyy-MM-ddTHH:mm:ss). */
  fechaHora: string;
  /** Identificador del cliente. */
  clienteId: number;
  /** Nombre completo del cliente. */
  clienteNombre: string;
  /** Identificador de la mascota. */
  mascotaId: number;
  /** Nombre de la mascota. */
  mascotaNombre: string;
  /** Identificador del veterinario. */
  veterinarioId: number;
  /** Nombre completo del veterinario. */
  veterinarioNombre: string;
  /** Estado de la cita. */
  estado: EstadoCita;
  /** Motivo de la consulta. */
  motivo: string;
  /** Si la cita ya generó una historia clínica. */
  tieneHistoria: boolean;
}

/** Datos para crear una cita. */
export interface CitaRequest {
  /** Cliente que solicita la cita. */
  clienteId: number;
  /** Mascota (debe pertenecer al cliente). */
  mascotaId: number;
  /** Veterinario asignado. */
  veterinarioId: number;
  /** Fecha y hora (yyyy-MM-ddTHH:mm:ss). */
  fechaHora: string;
  /** Motivo de la consulta. */
  motivo: string;
  /** Estado inicial. */
  estado: EstadoCita;
}

/** Datos editables de una cita (el cliente y la mascota no se modifican). */
export interface CitaActualizarRequest {
  /** Veterinario asignado. */
  veterinarioId: number;
  /** Fecha y hora (yyyy-MM-ddTHH:mm:ss). */
  fechaHora: string;
  /** Motivo de la consulta. */
  motivo: string;
  /** Nuevo estado. */
  estado: EstadoCita;
}

/** Criterios de filtrado del listado de citas (todos opcionales). */
export interface CitaFiltro {
  /** Texto contenido en el nombre del cliente. */
  cliente?: string;
  /** Texto contenido en el nombre de la mascota. */
  mascota?: string;
  /** Identificador exacto de la mascota. */
  mascotaId?: number | null;
  /** Identificador exacto del veterinario. */
  veterinarioId?: number | null;
  /** Estado de la cita ('' = todos). */
  estado?: EstadoCita | '';
  /** Fecha inicial (yyyy-MM-dd). */
  desde?: string;
  /** Fecha final (yyyy-MM-dd). */
  hasta?: string;
  /** Si es true, solo citas que aún no tienen historia clínica. */
  sinHistoria?: boolean;
}
