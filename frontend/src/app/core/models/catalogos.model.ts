/** Cliente (dueño de las mascotas). */
export interface Cliente {
  /** Identificador. */
  id: number;
  /** Tipo de documento (CC, TI, CE...). */
  tipoDocumento: string;
  /** Número de documento. */
  numeroDocumento: string;
  /** Nombres. */
  nombres: string;
  /** Apellidos. */
  apellidos: string;
  /** Nombres y apellidos. */
  nombreCompleto: string;
  /** Teléfono de contacto. */
  telefono: string | null;
  /** Dirección de residencia. */
  direccion: string | null;
  /** Si el cliente está activo. */
  activo: boolean;
}

/** Mascota perteneciente a un cliente. */
export interface Mascota {
  /** Identificador. */
  id: number;
  /** Nombre de la mascota. */
  nombre: string;
  /** Edad en años. */
  edad: number;
  /** Identificador de la raza. */
  razaId: number;
  /** Nombre de la raza. */
  raza: string;
  /** Especie. */
  especie: string;
  /** Identificador del dueño. */
  clienteId: number;
  /** Nombre completo del dueño. */
  clienteNombre: string;
  /** Si la mascota está activa. */
  activo: boolean;
}

/** Veterinario del centro. */
export interface Veterinario {
  /** Identificador. */
  id: number;
  /** Nombres y apellidos. */
  nombreCompleto: string;
  /** Registro profesional. */
  registroProfesional: string;
  /** Nombre de la especialización. */
  especializacion: string;
}

/** Medicamento disponible (no vencido) para formular. */
export interface Medicamento {
  /** Identificador. */
  id: number;
  /** Nombre del medicamento. */
  nombre: string;
  /** Presentación (tabletas, suspensión...). */
  presentacion: string | null;
  /** Fecha de vencimiento (yyyy-MM-dd). */
  fechaVence: string;
}
