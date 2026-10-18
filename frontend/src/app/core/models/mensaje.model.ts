/** Tipos de mensaje que se pueden mostrar al usuario. */
export type TipoMensaje = 'exito' | 'error' | 'info';

/**
 * Mensaje de éxito, error o información mostrado en pantalla.
 */
export interface Mensaje {
  /** Tipo del mensaje (define el color). */
  tipo: TipoMensaje;
  /** Texto comprensible para el usuario. */
  texto: string;
}
