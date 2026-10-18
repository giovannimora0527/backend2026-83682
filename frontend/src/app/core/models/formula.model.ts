/** Unidades de dosis que ofrece el formulario. */
export const UNIDADES_DOSIS: string[] = ['mg', 'g', 'ml', 'tabletas', 'cápsulas', 'gotas', 'UI'];

/** Vista RESUMIDA de una fórmula (la que se muestra en el listado). */
export interface FormulaResumen {
  /** Identificador. */
  id: number;
  /** Fecha y hora de creación del registro (yyyy-MM-ddTHH:mm:ss). */
  fechaCreacion: string;
  /** Fecha de emisión (yyyy-MM-dd). */
  fechaEmision: string;
  /** Nombre de la mascota. */
  mascotaNombre: string;
  /** Nombre completo del cliente. */
  clienteNombre: string;
  /** Nombre completo del veterinario. */
  veterinarioNombre: string;
  /** Nombre del medicamento. */
  medicamentoNombre: string;
  /** Texto resumido de dosis, frecuencia y duración. */
  posologia: string;
  /** Si la fórmula está vigente. */
  activo: boolean;
}

/** Vista COMPLETA de una fórmula (la que se muestra en "Ver" y en la edición). */
export interface FormulaDetalle {
  /** Identificador. */
  id: number;
  /** Identificador de la historia clínica asociada. */
  historiaId: number;
  /** Identificador de la cita de la consulta. */
  citaId: number;
  /** Fecha y hora de la consulta (yyyy-MM-ddTHH:mm:ss). */
  fechaConsulta: string;
  /** Motivo de la consulta. */
  motivoConsulta: string;
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
  /** Identificador del medicamento. */
  medicamentoId: number;
  /** Nombre del medicamento. */
  medicamentoNombre: string;
  /** Presentación del medicamento. */
  medicamentoPresentacion: string | null;
  /** Fecha de emisión (yyyy-MM-dd). */
  fechaEmision: string;
  /** Cantidad de la dosis. */
  dosisValor: number;
  /** Unidad de la dosis. */
  dosisUnidad: string;
  /** Cada cuántas horas se administra. */
  frecuenciaHoras: number;
  /** Duración del tratamiento en días. */
  duracionDias: number;
  /** Indicaciones para el cliente. */
  indicaciones: string;
  /** Si la fórmula está vigente. */
  activo: boolean;
  /** Fecha y hora de creación del registro. */
  fechaCreacion: string;
  /** Fecha y hora de la última actualización (null si nunca se editó). */
  fechaActualizacion: string | null;
}

/** Datos para registrar una fórmula. */
export interface FormulaRequest {
  /** Mascota a la que se formula. */
  mascotaId: number;
  /** Historia clínica / consulta (debe ser de esa mascota). */
  historiaId: number;
  /** Veterinario que emite la fórmula. */
  veterinarioId: number;
  /** Fecha de emisión (yyyy-MM-dd). */
  fechaEmision: string;
  /** Medicamento (no vencido). */
  medicamentoId: number;
  /** Cantidad de la dosis (mayor que cero). */
  dosisValor: number;
  /** Unidad de la dosis. */
  dosisUnidad: string;
  /** Cada cuántas horas se administra (1 a 168). */
  frecuenciaHoras: number;
  /** Duración del tratamiento en días (1 a 365). */
  duracionDias: number;
  /** Indicaciones para el cliente. */
  indicaciones: string;
}

/** Datos editables de una fórmula. */
export interface FormulaActualizarRequest {
  /** Medicamento (no vencido). */
  medicamentoId: number;
  /** Cantidad de la dosis (mayor que cero). */
  dosisValor: number;
  /** Unidad de la dosis. */
  dosisUnidad: string;
  /** Cada cuántas horas se administra (1 a 168). */
  frecuenciaHoras: number;
  /** Duración del tratamiento en días (1 a 365). */
  duracionDias: number;
  /** Indicaciones para el cliente. */
  indicaciones: string;
}

/** Criterios de filtrado del listado de fórmulas (todos opcionales). */
export interface FormulaFiltro {
  /** Texto contenido en el nombre de la mascota. */
  mascota?: string;
  /** Texto contenido en el nombre del cliente. */
  cliente?: string;
  /** Texto contenido en el nombre del medicamento. */
  medicamento?: string;
  /** 'true' solo vigentes, 'false' solo inactivas, '' todas. */
  activo?: 'true' | 'false' | '';
  /** Fecha de emisión inicial (yyyy-MM-dd). */
  desde?: string;
  /** Fecha de emisión final (yyyy-MM-dd). */
  hasta?: string;
}
