import { Component, DestroyRef, OnInit, computed, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Observable, forkJoin, of } from 'rxjs';
import { Cita, ETIQUETA_ESTADO_CITA, EstadoCita } from '../../../core/models/cita.model';
import { Cliente, Mascota, Veterinario } from '../../../core/models/catalogos.model';
import { Mensaje } from '../../../core/models/mensaje.model';
import { PageResponse } from '../../../core/models/page.model';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { CitaService } from '../../../core/services/cita.service';
import { ClienteService } from '../../../core/services/cliente.service';
import { estadosDisponibles } from '../../../core/utils/estado-cita';
import { combinarFechaHora, hoyIso, separarFechaHora } from '../../../core/utils/fechas';
import { mensajeDeError } from '../../../core/utils/mensaje-error';
import { fechaHoraNoPasada } from '../../../core/utils/validadores';
import { Alerta } from '../../../shared/alerta/alerta';

/**
 * Formulario de creación y edición de citas.
 *
 * Combos dependientes: al elegir un cliente se consultan únicamente sus mascotas (el usuario no puede
 * elegir una mascota de otro cliente). En la edición el cliente y la mascota no se pueden cambiar, y los
 * estados disponibles dependen del estado actual de la cita.
 *
 * Las validaciones críticas (cruces de horario, fecha pasada, pertenencia de la mascota) las repite y
 * garantiza el backend; aquí solo se validan para dar respuesta inmediata al usuario.
 */
@Component({
  selector: 'app-cita-form',
  imports: [ReactiveFormsModule, Alerta],
  templateUrl: './cita-form.html',
})
export class CitaForm implements OnInit {
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

  /** Cita que se edita; null cuando se está creando una cita nueva. */
  readonly cita = input<Cita | null>(null);

  /** Se emite con la cita guardada cuando el backend confirma la operación. */
  readonly guardado = output<Cita>();

  /** Se emite cuando el usuario cancela el formulario. */
  readonly cancelado = output<void>();

  /** True si el formulario está en modo edición. */
  readonly esEdicion = computed(() => this.cita() !== null);

  /** Estados que se pueden elegir según el estado actual de la cita. */
  readonly estados = computed(() => estadosDisponibles(this.cita()?.estado ?? null));

  /** Texto legible de cada estado. */
  readonly etiquetaEstado = ETIQUETA_ESTADO_CITA;

  /** Fecha de hoy, mínimo permitido al crear una cita. */
  readonly hoy = hoyIso();

  /** Clientes disponibles en el combo. */
  readonly clientes = signal<Cliente[]>([]);

  /** Mascotas del cliente seleccionado. */
  readonly mascotas = signal<Mascota[]>([]);

  /** Veterinarios disponibles en el combo. */
  readonly veterinarios = signal<Veterinario[]>([]);

  /** Indica que se están cargando los catálogos iniciales. */
  readonly cargandoCatalogos = signal(true);

  /** Indica que se están consultando las mascotas del cliente. */
  readonly cargandoMascotas = signal(false);

  /** Indica que se está enviando el formulario al backend. */
  readonly guardando = signal(false);

  /** Mensaje de error o información del formulario. */
  readonly mensaje = signal<Mensaje | null>(null);

  /** Se activa al pulsar Guardar para mostrar los errores de los campos aún no tocados. */
  readonly intentoEnvio = signal(false);

  /** Formulario reactivo con sus validaciones. */
  readonly form = this.fb.group(
    {
      clienteId: this.fb.control<number | null>(null, Validators.required),
      mascotaId: this.fb.control<number | null>(null, Validators.required),
      veterinarioId: this.fb.control<number | null>(null, Validators.required),
      fecha: this.fb.nonNullable.control('', Validators.required),
      hora: this.fb.nonNullable.control('', Validators.required),
      motivo: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(500)]),
      estado: this.fb.nonNullable.control<EstadoCita>('PENDIENTE', Validators.required),
    },
    { validators: [fechaHoraNoPasada(() => this.cita()?.fechaHora ?? null)] },
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

  /** Inicializa el formulario (modo edición o creación) y carga los catálogos. */
  ngOnInit(): void {
    const cita = this.cita();
    if (cita) {
      const { fecha, hora } = separarFechaHora(cita.fechaHora);
      this.form.patchValue({
        clienteId: cita.clienteId,
        mascotaId: cita.mascotaId,
        veterinarioId: cita.veterinarioId,
        fecha,
        hora,
        motivo: cita.motivo,
        estado: cita.estado,
      });
      // El cliente y la mascota no se modifican: de ellos dependen las historias y fórmulas de la cita.
      this.form.controls.clienteId.disable();
      this.form.controls.mascotaId.disable();
    } else {
      this.form.controls.clienteId.valueChanges
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((clienteId) => this.alCambiarCliente(clienteId));
    }
    this.cargarCatalogos();
  }

  /** Carga en paralelo los clientes (solo al crear) y los veterinarios. */
  private cargarCatalogos(): void {
    const clientes$: Observable<PageResponse<Cliente> | null> = this.esEdicion()
      ? of(null)
      : this.clienteService.listar();

    forkJoin({ clientes: clientes$, veterinarios: this.catalogoService.listarVeterinarios() }).subscribe({
      next: ({ clientes, veterinarios }) => {
        this.clientes.set(clientes?.contenido ?? []);
        this.veterinarios.set(veterinarios);
        this.cargandoCatalogos.set(false);
      },
      error: (error) => {
        this.cargandoCatalogos.set(false);
        this.mensaje.set({ tipo: 'error', texto: mensajeDeError(error) });
      },
    });
  }

  /**
   * Combo dependiente: cuando cambia el cliente se limpia la mascota y se consultan solo sus mascotas.
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
   * Indica si un campo debe mostrar su error (inválido y tocado, o ya se intentó enviar).
   *
   * @param nombre nombre del control
   * @returns true si hay que mostrar el mensaje de error
   */
  invalido(nombre: 'clienteId' | 'mascotaId' | 'veterinarioId' | 'fecha' | 'hora' | 'motivo' | 'estado'): boolean {
    const control = this.form.controls[nombre];
    return control.invalid && (control.touched || this.intentoEnvio());
  }

  /**
   * Indica si debe mostrarse el error de "fecha pasada".
   *
   * @returns true si la fecha y hora elegidas ya pasaron
   */
  fechaPasada(): boolean {
    return !!this.form.errors?.['fechaPasada'] && (this.form.touched || this.intentoEnvio());
  }

  /** Valida el formulario y lo envía al backend (crear o editar según el modo). */
  guardar(): void {
    this.intentoEnvio.set(true);
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      this.mensaje.set({ tipo: 'error', texto: 'Revise los campos marcados antes de continuar.' });
      return;
    }

    const valores = this.form.getRawValue();
    const fechaHora = combinarFechaHora(valores.fecha, valores.hora);
    const citaActual = this.cita();
    this.guardando.set(true);
    this.mensaje.set(null);

    const peticion =
      citaActual === null
        ? this.citaService.crear({
            clienteId: valores.clienteId as number,
            mascotaId: valores.mascotaId as number,
            veterinarioId: valores.veterinarioId as number,
            fechaHora,
            motivo: valores.motivo.trim(),
            estado: valores.estado,
          })
        : this.citaService.actualizar(citaActual.id, {
            veterinarioId: valores.veterinarioId as number,
            fechaHora,
            motivo: valores.motivo.trim(),
            estado: valores.estado,
          });

    peticion.subscribe({
      next: (guardada) => {
        this.guardando.set(false);
        this.guardado.emit(guardada);
      },
      error: (error) => {
        this.guardando.set(false);
        this.mensaje.set({ tipo: 'error', texto: mensajeDeError(error) });
      },
    });
  }
}
