import { Component, input } from '@angular/core';
import { FormulaDetalle } from '../../../core/models/formula.model';
import { formatearFecha, formatearFechaHora } from '../../../core/utils/fechas';

/**
 * Vista completa de una fórmula (botón «Ver»). A diferencia del listado, que solo muestra un resumen,
 * aquí aparecen todos los datos: consulta asociada, medicamento, posología, indicaciones y auditoría.
 */
@Component({
  selector: 'app-formula-detalle',
  templateUrl: './formula-detalle.html',
})
export class FormulaDetalleVista {
  /** Fórmula que se muestra. */
  readonly formula = input.required<FormulaDetalle>();

  /**
   * Fecha con formato legible, para la plantilla.
   *
   * @param iso fecha ISO
   * @returns fecha dd/MM/yyyy
   */
  fecha(iso: string): string {
    return formatearFecha(iso);
  }

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
