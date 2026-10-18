import { Component, input } from '@angular/core';
import { ETIQUETA_ESTADO_HISTORIA, Historia } from '../../../core/models/historia.model';
import { formatearFechaHora } from '../../../core/utils/fechas';
import { AnotacionesPanel } from '../anotaciones-panel/anotaciones-panel';

/**
 * Detalle de una historia clínica (botón «Ver»): datos de la consulta y, debajo, el histórico de
 * anotaciones en un DataTable.
 */
@Component({
  selector: 'app-historia-detalle',
  imports: [AnotacionesPanel],
  templateUrl: './historia-detalle.html',
})
export class HistoriaDetalle {
  /** Historia clínica que se muestra. */
  readonly historia = input.required<Historia>();

  /** Texto legible de cada estado de historia. */
  readonly etiquetaEstado = ETIQUETA_ESTADO_HISTORIA;

  /**
   * Fecha y hora con formato legible, para la plantilla.
   *
   * @param iso fecha-hora ISO
   * @returns fecha y hora legibles
   */
  fechaHora(iso: string): string {
    return formatearFechaHora(iso);
  }
}
