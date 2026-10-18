import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Veterinario } from '../../../core/models/catalogos.model';
import { Cita, CitaFiltro, ESTADOS_CITA, ETIQUETA_ESTADO_CITA, EstadoCita } from '../../../core/models/cita.model';
import { Mensaje } from '../../../core/models/mensaje.model';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { CitaService } from '../../../core/services/cita.service';
import { ControlTabla } from '../../../core/utils/control-tabla';
import { citaEditable, motivoCitaNoEditable } from '../../../core/utils/estado-cita';
import { formatearFecha, formatearHora } from '../../../core/utils/fechas';
import { mensajeDeError } from '../../../core/utils/mensaje-error';
import { Alerta } from '../../../shared/alerta/alerta';
import { Modal } from '../../../shared/modal/modal';
import { Columna, TablaDatos } from '../../../shared/tabla-datos/tabla-datos';
import { CitaForm } from '../cita-form/cita-form';

/** Clase CSS de la insignia de cada estado de cita. */
const COLOR_ESTADO: Record<EstadoCita, string> = {
  PENDIENTE: 'badge-amarillo',
  CONFIRMADA: 'badge-azul',
  ATENDIDA: 'badge-verde',
  CANCELADA: 'badge-rojo',
  PROGRAMADA: 'badge-morado',
};

/**
 * Pantalla de gestión de citas médicas: DataTable con filtros, ordenamiento y paginación
 * (inicialmente por fecha y hora descendente) y formulario de creación/edición en una ventana modal.
 */
@Component({
  selector: 'app-citas-page',
  imports: [ReactiveFormsModule, TablaDatos, Modal, Alerta, CitaForm],
  templateUrl: './citas-page.html',
})
export class CitasPage implements OnInit {
  /** Constructor de formularios reactivos. */
  private readonly fb = inject(FormBuilder);

  /** Servicio de citas. */
  private readonly citaService = inject(CitaService);

  /** Servicio de catálogos (veterinarios del filtro). */
  private readonly catalogoService = inject(CatalogoService);

  /** Orden inicial exigido por el enunciado: fecha y hora de la cita, descendente. */
  readonly tabla = new ControlTabla('fechaHora', 'desc');

  /** Estados disponibles en el filtro. */
  readonly estados = ESTADOS_CITA;

  /** Texto legible de cada estado. */
  readonly etiquetaEstado = ETIQUETA_ESTADO_CITA;

  /** Veterinarios disponibles en el filtro. */
  readonly veterinarios = signal<Veterinario[]>([]);

  /** Filas de la página actual. */
  readonly filas = signal<Cita[]>([]);

  /** Total de citas que cumplen el filtro. */
  readonly total = signal(0);

  /** Indica que se está consultando el backend. */
  readonly cargando = signal(false);

  /** Mensaje de éxito o error de la pantalla. */
  readonly mensaje = signal<Mensaje | null>(null);

  /** Si el formulario modal está abierto. */
  readonly formularioAbierto = signal(false);

  /** Cita que se está editando (null = cita nueva). */
  readonly citaEnEdicion = signal<Cita | null>(null);

  /** Formulario de filtros. */
  readonly filtroForm = this.fb.group({
    cliente: this.fb.nonNullable.control(''),
    mascota: this.fb.nonNullable.control(''),
    veterinarioId: this.fb.control<number | null>(null),
    estado: this.fb.nonNullable.control<EstadoCita | ''>(''),
    desde: this.fb.nonNullable.control(''),
    hasta: this.fb.nonNullable.control(''),
  });

  /** Columnas del DataTable (Fecha, Hora, Cliente, Mascota, Veterinario y Estado como mínimo). */
  readonly columnas: Columna<Cita>[] = [
    { id: 'fecha', titulo: 'Fecha', claveOrden: 'fechaHora', valor: (c) => formatearFecha(c.fechaHora) },
    { id: 'hora', titulo: 'Hora', claveOrden: 'fechaHora', valor: (c) => formatearHora(c.fechaHora) },
    { id: 'cliente', titulo: 'Cliente', claveOrden: 'cliente', valor: (c) => c.clienteNombre },
    { id: 'mascota', titulo: 'Mascota', claveOrden: 'mascota', valor: (c) => c.mascotaNombre },
    { id: 'veterinario', titulo: 'Veterinario', claveOrden: 'veterinario', valor: (c) => c.veterinarioNombre },
    {
      id: 'estado',
      titulo: 'Estado',
      claveOrden: 'estado',
      valor: (c) => ETIQUETA_ESTADO_CITA[c.estado],
      insignia: (c) => COLOR_ESTADO[c.estado],
    },
  ];

  /** Carga el listado inicial y el catálogo de veterinarios para el filtro. */
  ngOnInit(): void {
    this.cargar();
    this.catalogoService.listarVeterinarios().subscribe({
      next: (veterinarios) => this.veterinarios.set(veterinarios),
      error: (error) => this.mensaje.set({ tipo: 'error', texto: mensajeDeError(error) }),
    });
  }

  /** Consulta al backend la página actual con los filtros, el orden y la paginación vigentes. */
  cargar(): void {
    const f = this.filtroForm.getRawValue();
    const filtro: CitaFiltro = {
      cliente: f.cliente.trim(),
      mascota: f.mascota.trim(),
      veterinarioId: f.veterinarioId,
      estado: f.estado,
      desde: f.desde,
      hasta: f.hasta,
    };

    this.cargando.set(true);
    this.citaService.listar(filtro, this.tabla.parametros()).subscribe({
      next: (pagina) => {
        this.filas.set(pagina.contenido);
        this.total.set(pagina.totalElementos);
        this.cargando.set(false);
      },
      error: (error) => {
        this.cargando.set(false);
        this.mensaje.set({ tipo: 'error', texto: mensajeDeError(error) });
      },
    });
  }

  /** Aplica los filtros (vuelve a la primera página). */
  buscar(): void {
    const { desde, hasta } = this.filtroForm.getRawValue();
    if (desde && hasta && desde > hasta) {
      this.mensaje.set({ tipo: 'error', texto: 'La fecha inicial no puede ser mayor que la fecha final.' });
      return;
    }
    this.mensaje.set(null);
    this.tabla.reiniciarPagina();
    this.cargar();
  }

  /** Limpia todos los filtros y vuelve a consultar. */
  limpiar(): void {
    this.filtroForm.reset();
    this.mensaje.set(null);
    this.tabla.reiniciarPagina();
    this.cargar();
  }

  /** Abre el formulario para registrar una cita nueva. */
  nueva(): void {
    this.citaEnEdicion.set(null);
    this.formularioAbierto.set(true);
  }

  /**
   * Abre el formulario para editar una cita (solo si su estado lo permite).
   *
   * @param cita cita a editar
   */
  editar(cita: Cita): void {
    if (!citaEditable(cita.estado)) {
      return;
    }
    this.citaEnEdicion.set(cita);
    this.formularioAbierto.set(true);
  }

  /** Cierra el formulario sin guardar. */
  cerrarFormulario(): void {
    this.formularioAbierto.set(false);
    this.citaEnEdicion.set(null);
  }

  /**
   * Se ejecuta cuando el backend confirma que la cita se guardó.
   *
   * @param cita cita guardada
   */
  alGuardar(cita: Cita): void {
    const esNueva = this.citaEnEdicion() === null;
    this.cerrarFormulario();
    this.mensaje.set({
      tipo: 'exito',
      texto: esNueva ? `Cita #${cita.id} registrada correctamente.` : `Cita #${cita.id} actualizada correctamente.`,
    });
    this.cargar();
  }

  /**
   * Reactiva una cita cancelada (operación explícita, con confirmación).
   *
   * @param cita cita cancelada
   */
  reactivar(cita: Cita): void {
    if (!window.confirm(`¿Reactivar la cita de ${cita.mascotaNombre}? Quedará en estado Pendiente.`)) {
      return;
    }
    this.citaService.reactivar(cita.id).subscribe({
      next: () => {
        this.mensaje.set({ tipo: 'exito', texto: 'La cita fue reactivada y quedó en estado Pendiente.' });
        this.cargar();
      },
      error: (error) => this.mensaje.set({ tipo: 'error', texto: mensajeDeError(error) }),
    });
  }

  /**
   * Indica si el botón Editar debe estar habilitado.
   *
   * @param cita cita de la fila
   * @returns true si la cita puede editarse
   */
  puedeEditar(cita: Cita): boolean {
    return citaEditable(cita.estado);
  }

  /**
   * Texto de ayuda del botón Editar cuando está deshabilitado.
   *
   * @param cita cita de la fila
   * @returns explicación de la restricción o cadena vacía
   */
  ayudaEditar(cita: Cita): string {
    return motivoCitaNoEditable(cita.estado);
  }
}
