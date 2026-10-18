import { Component, DestroyRef, OnInit, inject, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Cita } from '../../../core/models/cita.model';
import { Cliente, Mascota, Veterinario } from '../../../core/models/catalogos.model';
import { Historia } from '../../../core/models/historia.model';
import { Mensaje } from '../../../core/models/mensaje.model';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { CitaService } from '../../../core/services/cita.service';
import { ClienteService } from '../../../core/services/cliente.service';
import { HistoriaService } from '../../../core/services/historia.service';
import { ahoraIso, combinarFechaHora, formatearFechaHora, separarFechaHora } from '../../../core/utils/fechas';
import { mensajeDeError } from '../../../core/utils/mensaje-error';
import { fechaHoraNoFutura } from '../../../core/utils/validadores';
import { Alerta } from '../../../shared/alerta/alerta';

/**
 * Formulario de creación de historias clínicas.
 *
 * Combos dependientes: Cliente → Mascotas del cliente → Citas ATENDIDAS de esa mascota que aún no tienen
 * historia. Así el usuario no puede seleccionar manualmente una mascota que no pertenezca al cliente ni una
 * cita de otra mascota. Al elegir la cita se precargan el veterinario, la fecha, la hora y el motivo.
 */
@Component({
  selector: 'app-historia-form',
  imports: [ReactiveFormsModule, Alerta],
  templateUrl: './historia-form.html',
})
export class HistoriaForm implements OnInit {
  /** Constructor de formularios reactivos. */
  private readonly fb = inject(FormBuilder);

  /** Permite cancelar suscripciones cuando el componente se destruye. */
  private readonly destroyRef = inject(DestroyRef);

  /** Servicio de clientes y mascotas. */
  private readonly clienteService = inject(ClienteService);

  /** Servicio de catálogos (veterinarios). */
  private readonly catalogoService = inject(CatalogoService);

  /** Servicio de citas. */
  private readonly citaService = inject(CitaService);

  /** Servicio de historias clínicas. */
  private readonly historiaService = inject(HistoriaService);

  /** Se emite con la historia guardada cuando el backend confirma la operación. */
  readonly guardado = output<Historia>();

  /** Se emite cuando el usuario cancela el formulario. */
  readonly cancelado = output<void>();

  /** Clientes disponibles en el combo. */
  readonly clientes = signal<Cliente[]>([]);

  /** Mascotas del cliente seleccionado. */
  readonly mascotas = signal<Mascota[]>([]);

  /** Citas atendidas, sin historia, de la mascota seleccionada. */
  readonly citas = signal<Cita[]>([]);

  /** Veterinarios disponibles en el combo. */
  readonly veterinarios = signal<Veterinario[]>([]);

  /** Indica que se están cargando los catálogos iniciales. */
  readonly cargandoCatalogos = signal(true);

  /** Indica que se están consultando las mascotas del cliente. */
  readonly cargandoMascotas = signal(false);

  /** Indica que se están consultando las citas de la mascota. */
  readonly cargandoCitas = signal(false);

  /** Indica que se está enviando el formulario al backend. */
  readonly guardando = signal(false);

  /** Mensaje de error del formulario. */
  readonly mensaje = signal<Mensaje | null>(null);

  /** Se activa al pulsar Guardar para mostrar los errores de los campos aún no tocados. */
  readonly intentoEnvio = signal(false);

  /** Formulario reactivo con sus validaciones. */
  readonly form = this.fb.group(
    {
      clienteId: this.fb.control<number | null>(null, Validators.required),
      mascotaId: this.fb.control<number | null>(null, Validators.required),
      citaId: this.fb.control<number | null>(null, Validators.required),
      veterinarioId: this.fb.control<number | null>(null, Validators.required),
      fecha: this.fb.nonNullable.control('', Validators.required),
      hora: this.fb.nonNullable.control('', Validators.required),
      motivo: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(500)]),
      diagnostico: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(2000)]),
      observaciones: this.fb.nonNullable.control('', Validators.maxLength(2000)),
    },
    { validators: [fechaHoraNoFutura()] },
  );

  /**
   * Texto de ayuda del combo de mascotas según el estado de la carga.
   *
   * @returns texto de la opción inicial del combo
   */
  textoMascota(): string {
    if (this.form.controls.clienteId.value === null) {
      return 'Seleccione primero un cliente';
    }
    if (this.cargandoMascotas()) {
      return 'Cargando mascotas…';
    }
    return this.mascotas().length === 0 ? 'El cliente no tiene mascotas registradas' : 'Seleccione una mascota';
  }

  /**
   * Texto de ayuda del combo de citas según el estado de la carga.
   *
   * @returns texto de la opción inicial del combo
   */
  textoCita(): string {
    if (this.form.controls.mascotaId.value === null) {
      return 'Seleccione primero una mascota';
    }
    if (this.cargandoCitas()) {
      return 'Cargando citas…';
    }
    return this.citas().length === 0 ? 'La mascota no tiene citas atendidas sin historia' : 'Seleccione la cita atendida';
  }

  /**
   * Etiqueta de una cita en el combo.
   *
   * @param cita cita a describir
   * @returns fecha, hora y motivo de la cita
   */
  etiquetaCita(cita: Cita): string {
    return `${formatearFechaHora(cita.fechaHora)} — ${cita.motivo}`;
  }

  /** Configura los combos dependientes y carga los catálogos iniciales. */
  ngOnInit(): void {
    this.form.controls.clienteId.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((clienteId) => this.alCambiarCliente(clienteId));
    this.form.controls.mascotaId.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((mascotaId) => this.alCambiarMascota(mascotaId));
    this.form.controls.citaId.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((citaId) => this.alCambiarCita(citaId));

    this.clienteService.listar().subscribe({
      next: (pagina) => {
        this.clientes.set(pagina.contenido);
        this.cargandoCatalogos.set(false);
      },
      error: (error) => {
        this.cargandoCatalogos.set(false);
        this.mensaje.set({ tipo: 'error', texto: mensajeDeError(error) });
      },
    });
    this.catalogoService.listarVeterinarios().subscribe({
      next: (veterinarios) => this.veterinarios.set(veterinarios),
      error: (error) => this.mensaje.set({ tipo: 'error', texto: mensajeDeError(error) }),
    });
  }

  /**
   * Combo dependiente 1: al cambiar el cliente se limpian mascota y cita, y se consultan sus mascotas.
   *
   * @param clienteId cliente seleccionado (null si se limpió la selección)
   */
  private alCambiarCliente(clienteId: number | null): void {
    this.form.controls.mascotaId.setValue(null);
    this.mascotas.set([]);
    if (clienteId === null) {
      return;
    }
    this.cargandoMascotas.set(true);
    this.clienteService.listarMascotas(clienteId).subscribe({
      next: (mascotas) => {
        this.mascotas.set(mascotas);
        this.cargandoMascotas.set(false);
      },
      error: (error) => {
        this.cargandoMascotas.set(false);
        this.mensaje.set({ tipo: 'error', texto: mensajeDeError(error) });
      },
    });
  }

  /**
   * Combo dependiente 2: al cambiar la mascota se limpia la cita y se consultan sus citas atendidas sin historia.
   *
   * @param mascotaId mascota seleccionada (null si se limpió la selección)
   */
  private alCambiarMascota(mascotaId: number | null): void {
    this.form.controls.citaId.setValue(null);
    this.citas.set([]);
    if (mascotaId === null) {
      return;
    }
    this.cargandoCitas.set(true);
    this.citaService
      .listar(
        { mascotaId, estado: 'ATENDIDA', sinHistoria: true },
        { pagina: 0, tamano: 100, orden: 'fechaHora', direccion: 'desc' },
      )
      .subscribe({
        next: (pagina) => {
          this.citas.set(pagina.contenido);
          this.cargandoCitas.set(false);
        },
        error: (error) => {
          this.cargandoCitas.set(false);
          this.mensaje.set({ tipo: 'error', texto: mensajeDeError(error) });
        },
      });
  }

  /**
   * Al elegir la cita se precargan el veterinario, la fecha, la hora y el motivo (el usuario puede ajustarlos).
   * Si la cita está programada a futuro, la consulta se precarga con la fecha y hora actuales.
   *
   * @param citaId cita seleccionada (null si se limpió la selección)
   */
  private alCambiarCita(citaId: number | null): void {
    const cita = this.citas().find((c) => c.id === citaId);
    if (!cita) {
      return;
    }
    const ahora = ahoraIso();
    const base = cita.fechaHora.slice(0, 16) > ahora ? ahora : cita.fechaHora;
    const { fecha, hora } = separarFechaHora(base);
    this.form.patchValue({ veterinarioId: cita.veterinarioId, fecha, hora, motivo: cita.motivo });
  }

  /**
   * Indica si un campo debe mostrar su error (inválido y tocado, o ya se intentó enviar).
   *
   * @param nombre nombre del control
   * @returns true si hay que mostrar el mensaje de error
   */
  invalido(
    nombre: 'clienteId' | 'mascotaId' | 'citaId' | 'veterinarioId' | 'fecha' | 'hora' | 'motivo' | 'diagnostico' | 'observaciones',
  ): boolean {
    const control = this.form.controls[nombre];
    return control.invalid && (control.touched || this.intentoEnvio());
  }

  /**
   * Indica si debe mostrarse el error de "fecha futura".
   *
   * @returns true si la fecha y hora de la consulta son futuras
   */
  fechaFutura(): boolean {
    return !!this.form.errors?.['fechaFutura'] && (this.form.touched || this.intentoEnvio());
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
      .crear({
        clienteId: valores.clienteId as number,
        mascotaId: valores.mascotaId as number,
        citaId: valores.citaId as number,
        veterinarioId: valores.veterinarioId as number,
        fechaHora: combinarFechaHora(valores.fecha, valores.hora),
        motivo: valores.motivo.trim(),
        diagnostico: valores.diagnostico.trim(),
        observaciones: valores.observaciones.trim(),
      })
      .subscribe({
        next: (historia) => {
          this.guardando.set(false);
          this.guardado.emit(historia);
        },
        error: (error) => {
          this.guardando.set(false);
          this.mensaje.set({ tipo: 'error', texto: mensajeDeError(error) });
        },
      });
  }
}
