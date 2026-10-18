import { Component, input, output } from '@angular/core';
import { Mensaje } from '../../core/models/mensaje.model';

/**
 * Muestra un mensaje de éxito, error o información. No muestra nada si el mensaje es null.
 */
@Component({
  selector: 'app-alerta',
  template: `
    @if (mensaje(); as m) {
      <div
        class="alerta"
        [class.alerta-exito]="m.tipo === 'exito'"
        [class.alerta-error]="m.tipo === 'error'"
        [class.alerta-info]="m.tipo === 'info'"
        [attr.role]="m.tipo === 'error' ? 'alert' : 'status'"
      >
        <span>{{ m.texto }}</span>
        <button type="button" class="btn-icono" aria-label="Cerrar mensaje" (click)="cerrar.emit()">✕</button>
      </div>
    }
  `,
})
export class Alerta {
  /** Mensaje a mostrar (null = oculto). */
  readonly mensaje = input<Mensaje | null>(null);

  /** Se emite cuando el usuario cierra el mensaje. */
  readonly cerrar = output<void>();
}
