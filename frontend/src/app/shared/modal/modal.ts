import { Component, input, output } from '@angular/core';

/**
 * Ventana modal genérica. El contenido se coloca dentro de la etiqueta {@code <app-modal>}.
 * Solo se cierra con el botón ✕ (no al hacer clic fuera) para no perder datos de un formulario por error.
 */
@Component({
  selector: 'app-modal',
  template: `
    <div class="modal-fondo">
      <div class="modal" [class.modal-ancho]="ancho()" role="dialog" aria-modal="true" [attr.aria-label]="titulo()">
        <header class="modal-cabecera">
          <h2>{{ titulo() }}</h2>
          <button type="button" class="btn-icono" aria-label="Cerrar" (click)="cerrar.emit()">✕</button>
        </header>
        <div class="modal-cuerpo">
          <ng-content></ng-content>
        </div>
      </div>
    </div>
  `,
})
export class Modal {
  /** Título de la ventana. */
  readonly titulo = input('');

  /** Si es true, la ventana usa un ancho mayor (para detalles con tablas). */
  readonly ancho = input(false);

  /** Se emite cuando el usuario pulsa el botón de cerrar. */
  readonly cerrar = output<void>();
}
