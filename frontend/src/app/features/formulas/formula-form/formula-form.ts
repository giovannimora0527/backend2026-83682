import { Component, DestroyRef, OnInit, computed, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Observable, forkJoin, of } from 'rxjs';
import { Cliente, Mascota, Medicamento, Veterinario } from '../../../core/models/catalogos.model';
import { FormulaDetalle, UNIDADES_DOSIS } from '../../../core/models/formula.model';
import { Historia } from '../../../core/models/historia.model';
import { Mensaje } from '../../../core/models/mensaje.model';
import { PageResponse } from '../../../core/models/page.model';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { ClienteService } from '../../../core/services/cliente.service';
import { FormulaService } from '../../../core/services/formula.service';
import { HistoriaService } from '../../../core/services/historia.service';
import { formatearFecha, formatearFechaHora, hoyIso } from '../../../core/utils/fechas';
import { mensajeDeError } from '../../../core/utils/mensaje-error';
import { Alerta } from '../../../shared/alerta/alerta';

/**
 * Formulario de creación y edición de fórmulas médicas.
 *
 * Combos dependientes: Cliente → Mascotas del cliente → Consultas (historias abiertas) de esa mascota.
 * Así no es posible asociar la fórmula a una consulta de otra mascota (regla de negocio 7).
 * En la edición solo se pueden modificar medicamento, dosis, frecuencia, duración e indicaciones:
 * la mascota, la consulta, el veterinario y la fecha de emisión forman parte del registro clínico.
 */
@Component({
  selector: 'app-formula-form',
  imports: [ReactiveFormsModule, Alerta],
  templateUrl: './formula-form.html',
})
export class FormulaForm implements OnInit {
  /** Constructor de formularios reactivos. */
  private readonly fb = inject(FormBuilder);

  /** Permite cancelar suscripciones cuando el componente se destruye. */
  private readonly destroyRef = inject(DestroyRef);

  /** Servicio de clientes y mascotas. */
  private readonly clienteService = inject(ClienteService);

  /** Servicio de catálogos (veterinarios y medicamentos). */
  private readonly catalogoService = inject(CatalogoService);

  /** Servicio de historias clínicas. */
  private readonly historiaService = inject(HistoriaService);

  /** Servicio de fórmulas. */
  private readonly formulaService = inject(FormulaService);

  /** Fórmula que se edita; null cuando se está creando una fórmula nueva. */
  readonly formula = input<FormulaDetalle | null>(null);

  /** Se emite con la fórmula guardada cuando el backend confirma la operación. */
  readonly guardado = output<FormulaDetalle>();

  /** Se emite cuando el usuario cancela el formulario. */
  readonly cancelado = output<void>();

  /** True si el formulario está en modo edición. */
  readonly esEdicion = computed(() => this.formula() !== null);

  /** Unidades de dosis disponibles (incluye la de la fórmula editada si no está en la lista base). */
  readonly unidades = computed(() => {
    const actual = this.formula()?.dosisUnidad;
    return actual && !UNIDADES_DOSIS.includes(actual) ? [...UNIDADES_DOSIS, actual] : UNIDADES_DOSIS;
  });

  /** Fecha de hoy, máximo permitido para la fecha de emisión. */
  readonly hoy = hoyIso();

  /** Clientes disponibles en el combo. */
  readonly clientes = signal<Cliente[]>([]);

  /** Mascotas del cliente seleccionado. */
  readonly mascotas = signal<Mascota[]>([]);

  /** Historias abiertas de la mascota seleccionada. */
  readonly historias = signal<Historia[]>([]);

  /** Veterinarios disponibles en el combo. */
  readonly veterinarios = signal<Veterinario[]>([]);

  /** Medicamentos vigentes disponibles en el combo. */
  readonly medicamentos = signal<Medicamento[]>([]);

  /** Fecha (yyyy-MM-dd) de la consulta seleccionada: la emisión no puede ser anterior a ella. */
  readonly fechaConsulta = signal<string | null>(null);

  /** Indica que se están cargando los catálogos iniciales. */
  readonly cargandoCatalogos = signal(true);

  /** Indica que se están consultando las mascotas del cliente. */
  readonly cargandoMascotas = signal(false);

  /** Indica que se están consultando las historias de la mascota. */
  readonly cargandoHistorias = signal(false);

  /** Indica que se está enviando el formulario al backend. */
  readonly guardando = signal(false);

  /** Mensaje de error del formulario. */
  readonly mensaje = signal<Mensaje | null>(null);

  /** Se activa al pulsar Guardar para mostrar los errores de los campos aún no tocados. */
  readonly intentoEnvio = signal(false);

  /**
   * Validador de la fecha de emisión: no puede ser futura ni anterior a la fecha de la consulta
   * (regla de negocio 4). Se declara antes del formulario porque este lo usa al crearse.
   *
   * @param control control de la fecha de emisión
   * @returns errores de validación o null si la fecha es válida
   */
  private readonly validarFechaEmision = (control: AbstractControl): ValidationErrors | null => {
    const valor = control.value as string | null;
    if (!valor) {
      return null;
    }
    if (valor > hoyIso()) {
      return { fechaFutura: true };
    }
    const consulta = this.fechaConsulta();
    return consulta !== null && valor < consulta ? { anteriorConsulta: true } : null;
  };

  /** Formulario reactivo con sus validaciones (dosis y duración deben ser valores válidos: regla 5). */
  readonly form = this.fb.group({
    clienteId: this.fb.control<number | null>(null, Validators.required),
    mascotaId: this.fb.control<number | null>(null, Validators.required),
    historiaId: this.fb.control<number | null>(null, Validators.required),
    veterinarioId: this.fb.control<number | null>(null, Validators.required),
    fechaEmision: this.fb.nonNullable.control(hoyIso(), [Validators.required, this.validarFechaEmision]),
    medicamentoId: this.fb.control<number | null>(null, Validators.required),
    dosisValor: this.fb.control<number | null>(null, [
      Validators.required,
      Validators.min(0.01),
      Validators.max(10000),
    ]),
    dosisUnidad: this.fb.nonNullable.control('mg', [Validators.required, Validators.maxLength(20)]),
    frecuenciaHoras: this.fb.control<number | null>(null, [
      Validators.required,
      Validators.min(1),
      Validators.max(168),
      Validators.pattern(/^\d+$/),
    ]),
    duracionDias: this.fb.control<number | null>(null, [
      Validators.required,
      Validators.min(1),
      Validators.max(365),
      Validators.pattern(/^\d+$/),
    ]),
    indicaciones: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(1000)]),
  });

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
   * Texto de ayuda del combo de consultas según el estado de la carga.
   *
   * @returns texto de la opción inicial del combo
   */
  textoHistoria(): string {
    if (this.form.controls.mascotaId.value === null) {
      return 'Seleccione primero una mascota';
    }
    if (this.cargandoHistorias()) {
      return 'Cargando consultas…';
    }
    return this.historias().length === 0 ? 'La mascota no tiene historias abiertas' : 'Seleccione la consulta';
  }

  /**
   * Etiqueta de una historia en el combo.
   *
   * @param historia historia a describir
   * @returns fecha, hora y motivo de la consulta
   */
  etiquetaHistoria(historia: Historia): string {
    return `${formatearFechaHora(historia.fechaHora)} — ${historia.motivo}`;
  }

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

  /** Inicializa el formulario (modo edición o creación) y carga los catálogos. */
  ngOnInit(): void {
    const formula = this.formula();
    if (formula) {
      this.form.patchValue({
        clienteId: formula.clienteId,
        mascotaId: formula.mascotaId,
        historiaId: formula.historiaId,
        veterinarioId: formula.veterinarioId,
        fechaEmision: formula.fechaEmision,
        medicamentoId: formula.medicamentoId,
        dosisValor: formula.dosisValor,
        dosisUnidad: formula.dosisUnidad,
        frecuenciaHoras: formula.frecuenciaHoras,
        duracionDias: formula.duracionDias,
        indicaciones: formula.indicaciones,
      });
      // Datos que forman parte del registro clínico y no se modifican.
      this.form.controls.clienteId.disable();
      this.form.controls.mascotaId.disable();
      this.form.controls.historiaId.disable();
      this.form.controls.veterinarioId.disable();
      this.form.controls.fechaEmision.disable();
    } else {
      this.form.controls.clienteId.valueChanges
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((clienteId) => this.alCambiarCliente(clienteId));
      this.form.controls.mascotaId.valueChanges
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((mascotaId) => this.alCambiarMascota(mascotaId));
      this.form.controls.historiaId.valueChanges
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((historiaId) => this.alCambiarHistoria(historiaId));
    }
    this.cargarCatalogos();
  }

  /** Carga en paralelo los clientes (solo al crear), los veterinarios y los medicamentos vigentes. */
  private cargarCatalogos(): void {
    const clientes$: Observable<PageResponse<Cliente> | null> = this.esEdicion()
      ? of(null)
      : this.clienteService.listar();

    forkJoin({
      clientes: clientes$,
      veterinarios: this.catalogoService.listarVeterinarios(),
      medicamentos: this.catalogoService.listarMedicamentos(),
    }).subscribe({
      next: ({ clientes, veterinarios, medicamentos }) => {
        this.clientes.set(clientes?.contenido ?? []);
        this.veterinarios.set(veterinarios);
        this.medicamentos.set(this.incluirMedicamentoActual(medicamentos));
        this.cargandoCatalogos.set(false);
      },
      error: (error) => {
        this.cargandoCatalogos.set(false);
        this.mensaje.set({ tipo: 'error', texto: mensajeDeError(error) });
      },
    });
  }

  /**
   * En la edición, si el medicamento de la fórmula ya no está entre los vigentes (venció), se agrega a la lista
   * con una marca para que el combo no quede vacío; el backend impedirá guardarlo mientras siga vencido.
   *
   * @param vigentes medicamentos vigentes devueltos por el backend
   * @returns lista de medicamentos para el combo
   */
  private incluirMedicamentoActual(vigentes: Medicamento[]): Medicamento[] {
    const formula = this.formula();
    if (!formula || vigentes.some((m) => m.id === formula.medicamentoId)) {
      return vigentes;
    }
    const actual: Medicamento = {
      id: formula.medicamentoId,
      nombre: `${formula.medicamentoNombre} (vencido)`,
      presentacion: formula.medicamentoPresentacion,
      fechaVence: '',
    };
    return [actual, ...vigentes];
  }

  /**
   * Combo dependiente 1: al cambiar el cliente se limpian mascota y consulta, y se consultan sus mascotas.
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
   * Combo dependiente 2: al cambiar la mascota se limpia la consulta y se consultan solo sus historias abiertas.
   *
   * @param mascotaId mascota seleccionada (null si se limpió la selección)
   */
  private alCambiarMascota(mascotaId: number | null): void {
    this.form.controls.historiaId.setValue(null);
    this.historias.set([]);
    if (mascotaId === null) {
      return;
    }
    this.cargandoHistorias.set(true);
    this.historiaService
      .listar({ mascotaId, estado: 'ABIERTA' }, { pagina: 0, tamano: 100, orden: 'fechaHora', direccion: 'desc' })
      .subscribe({
        next: (pagina) => {
          this.historias.set(pagina.contenido);
          this.cargandoHistorias.set(false);
        },
        error: (error) => {
          this.cargandoHistorias.set(false);
          this.mensaje.set({ tipo: 'error', texto: mensajeDeError(error) });
        },
      });
  }

  /**
   * Al elegir la consulta se precarga el veterinario y se actualiza la fecha mínima de emisión.
   *
   * @param historiaId consulta seleccionada (null si se limpió la selección)
   */
  private alCambiarHistoria(historiaId: number | null): void {
    const historia = this.historias().find((h) => h.id === historiaId);
    this.fechaConsulta.set(historia ? historia.fechaHora.slice(0, 10) : null);
    if (historia) {
      this.form.controls.veterinarioId.setValue(historia.veterinarioId);
    }
    this.form.controls.fechaEmision.updateValueAndValidity();
  }

  /**
   * Indica si un campo debe mostrar su error (inválido y tocado, o ya se intentó enviar).
   *
   * @param nombre nombre del control
   * @returns true si hay que mostrar el mensaje de error
   */
  invalido(
    nombre:
      | 'clienteId'
      | 'mascotaId'
      | 'historiaId'
      | 'veterinarioId'
      | 'fechaEmision'
      | 'medicamentoId'
      | 'dosisValor'
      | 'dosisUnidad'
      | 'frecuenciaHoras'
      | 'duracionDias'
      | 'indicaciones',
  ): boolean {
    const control = this.form.controls[nombre];
    return control.invalid && (control.touched || this.intentoEnvio());
  }

  /** Valida el formulario y lo envía al backend (crear o editar según el modo). */
  guardar(): void {
    this.intentoEnvio.set(true);
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      this.mensaje.set({ tipo: 'error', texto: 'Revise los campos marcados antes de continuar.' });
      return;
    }

    const v = this.form.getRawValue();
    const formulaActual = this.formula();
    this.guardando.set(true);
    this.mensaje.set(null);

    const posologia = {
      medicamentoId: v.medicamentoId as number,
      dosisValor: v.dosisValor as number,
      dosisUnidad: v.dosisUnidad,
      frecuenciaHoras: v.frecuenciaHoras as number,
      duracionDias: v.duracionDias as number,
      indicaciones: v.indicaciones.trim(),
    };

    const peticion =
      formulaActual === null
        ? this.formulaService.crear({
            ...posologia,
            mascotaId: v.mascotaId as number,
            historiaId: v.historiaId as number,
            veterinarioId: v.veterinarioId as number,
            fechaEmision: v.fechaEmision,
          })
        : this.formulaService.actualizar(formulaActual.id, posologia);

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
