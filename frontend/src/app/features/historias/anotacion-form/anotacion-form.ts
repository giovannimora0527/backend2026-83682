import { Component, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Veterinario } from '../../../core/models/catalogos.model';
import { Anotacion } from '../../../core/models/historia.model';
import { Mensaje } from '../../../core/models/mensaje.model';
import { HistoriaService } from '../../../core/services/historia.service';
import { mensajeDeError } from '../../../core/utils/mensaje-error';
import { Alerta } from '../../../shared/alerta/alerta';

/**
 * Formulario para agregar una anotación a una historia clínica abierta.
 * La fecha y hora de la anotación la asigna el servidor, para que el historial no pueda alterarse.
 */
@Component({
  selector: 'app-anotacion-form',
  imports: [ReactiveFormsModule, Alerta],
  templateUrl: './anotacion-form.html',
})
export class AnotacionForm {
  /** Constructor de formularios reactivos. */
  private readonly fb = inject(FormBuilder);

  /** Servicio de historias clínicas y anotaciones. */
  private readonly historiaService = inject(HistoriaService);

  /** Identificador de la historia a la que se agrega la anotación. */
  readonly historiaId = input.required<number>();

  /** Veterinarios disponibles para elegir el autor. */
  readonly veterinarios = input<Veterinario[]>([]);

  /** Se emite con la anotación guardada cuando el backend confirma la operación. */
  readonly guardada = output<Anotacion>();

  /** Se emite cuando el usuario cancela el formulario. */
  readonly cancelada = output<void>();

  /** Indica que se está enviando el formulario al backend. */
  readonly guardando = signal(false);

  /** Mensaje de error del formulario. */
  readonly mensaje = signal<Mensaje | null>(null);

  /** Se activa al pulsar Guardar para mostrar los errores de los campos aún no tocados. */
  readonly intentoEnvio = signal(false);

  /** Formulario reactivo con sus validaciones. */
  readonly form = this.fb.group({
    veterinarioId: this.fb.control<number | null>(null, Validators.required),
    descripcion: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(2000)]),
    observacion: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(2000)]),
    diagnosticoEvolucion: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(2000)]),
  });

  /**
   * Indica si un campo debe mostrar su error (inválido y tocado, o ya se intentó enviar).
   *
   * @param nombre nombre del control
   * @returns true si hay que mostrar el mensaje de error
   */
  invalido(nombre: 'veterinarioId' | 'descripcion' | 'observacion' | 'diagnosticoEvolucion'): boolean {
    const control = this.form.controls[nombre];
    return control.invalid && (control.touched || this.intentoEnvio());
  }

  /** Valida el formulario y lo envía al backend. */
  guardar(): void {
    this.intentoEnvio.set(true);
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      this.mensaje.set({ tipo: 'error', texto: 'Revise los campos marcados antes de continuar.' });
      return;
    }

    const valores = this.form.getRawValue();
    this.guardando.set(true);
    this.mensaje.set(null);
    this.historiaService
      .crearAnotacion(this.historiaId(), {
        veterinarioId: valores.veterinarioId as number,
        descripcion: valores.descripcion.trim(),
        observacion: valores.observacion.trim(),
        diagnosticoEvolucion: valores.diagnosticoEvolucion.trim(),
      })
      .subscribe({
        next: (anotacion) => {
          this.guardando.set(false);
          this.guardada.emit(anotacion);
        },
        error: (error) => {
          this.guardando.set(false);
          this.mensaje.set({ tipo: 'error', texto: mensajeDeError(error) });
        },
      });
  }
}
