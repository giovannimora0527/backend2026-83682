import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Veterinario } from '../../../core/models/catalogos.model';
import {
  ETIQUETA_ESTADO_HISTORIA,
  EstadoHistoria,
  Historia,
  HistoriaFiltro,
} from '../../../core/models/historia.model';
import { Mensaje } from '../../../core/models/mensaje.model';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { HistoriaService } from '../../../core/services/historia.service';
import { ControlTabla } from '../../../core/utils/control-tabla';
import { formatearFechaHora } from '../../../core/utils/fechas';
import { mensajeDeError } from '../../../core/utils/mensaje-error';
import { Alerta } from '../../../shared/alerta/alerta';
import { Modal } from '../../../shared/modal/modal';
import { Columna, TablaDatos } from '../../../shared/tabla-datos/tabla-datos';
import { HistoriaDetalle } from '../historia-detalle/historia-detalle';
import { HistoriaForm } from '../historia-form/historia-form';

/**
 * Pantalla de gestión de historias clínicas: DataTable con filtros, ordenamiento y paginación
 * (inicialmente por fecha y hora de la consulta descendente), creación de historias y detalle con el
 * histórico de anotaciones.
 */
@Component({
  selector: 'app-historias-page',
  imports: [ReactiveFormsModule, TablaDatos, Modal, Alerta, HistoriaForm, HistoriaDetalle],
  templateUrl: './historias-page.html',
})
export class HistoriasPage implements OnInit {
  /** Constructor de formularios reactivos. */
  private readonly fb = inject(FormBuilder);

  /** Servicio de historias clínicas. */
  private readonly historiaService = inject(HistoriaService);

  /** Servicio de catálogos (veterinarios del filtro). */
  private readonly catalogoService = inject(CatalogoService);

  /** Orden inicial exigido por el enunciado: fecha y hora de la consulta, descendente. */
  readonly tabla = new ControlTabla('fechaHora', 'desc');

  /** Estados disponibles en el filtro. */
  readonly estados: EstadoHistoria[] = ['ABIERTA', 'CERRADA'];

  /** Texto legible de cada estado. */
  readonly etiquetaEstado = ETIQUETA_ESTADO_HISTORIA;

  /** Veterinarios disponibles en el filtro. */
  readonly veterinarios = signal<Veterinario[]>([]);

  /** Filas de la página actual. */
  readonly filas = signal<Historia[]>([]);

  /** Total de historias que cumplen el filtro. */
  readonly total = signal(0);

  /** Indica que se está consultando el backend. */
  readonly cargando = signal(false);

  /** Mensaje de éxito o error de la pantalla. */
  readonly mensaje = signal<Mensaje | null>(null);

  /** Si el formulario de nueva historia está abierto. */
  readonly formularioAbierto = signal(false);

  /** Historia cuyo detalle se está viendo (null = ninguna). */
  readonly historiaVista = signal<Historia | null>(null);

  /** Formulario de filtros. */
  readonly filtroForm = this.fb.group({
    cliente: this.fb.nonNullable.control(''),
    mascota: this.fb.nonNullable.control(''),
    motivo: this.fb.nonNullable.control(''),
    veterinarioId: this.fb.control<number | null>(null),
    estado: this.fb.nonNullable.control<EstadoHistoria | ''>(''),
    desde: this.fb.nonNullable.control(''),
    hasta: this.fb.nonNullable.control(''),
  });

  /** Columnas del DataTable (Fecha, Mascota, Cliente, Veterinario, Motivo de consulta y Estado como mínimo). */
  readonly columnas: Columna<Historia>[] = [
    { id: 'fecha', titulo: 'Fecha', claveOrden: 'fechaHora', valor: (h) => formatearFechaHora(h.fechaHora) },
    { id: 'mascota', titulo: 'Mascota', claveOrden: 'mascota', valor: (h) => h.mascotaNombre },
    { id: 'cliente', titulo: 'Cliente', claveOrden: 'cliente', valor: (h) => h.clienteNombre },
    { id: 'veterinario', titulo: 'Veterinario', claveOrden: 'veterinario', valor: (h) => h.veterinarioNombre },
    { id: 'motivo', titulo: 'Motivo de consulta', claveOrden: 'motivo', valor: (h) => h.motivo },
    {
      id: 'estado',
      titulo: 'Estado',
      claveOrden: 'estado',
      valor: (h) => ETIQUETA_ESTADO_HISTORIA[h.estado],
      insignia: (h) => (h.estado === 'ABIERTA' ? 'badge-verde' : 'badge-gris'),
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
    const filtro: HistoriaFiltro = {
      cliente: f.cliente.trim(),
      mascota: f.mascota.trim(),
      motivo: f.motivo.trim(),
      veterinarioId: f.veterinarioId,
      estado: f.estado,
      desde: f.desde,
      hasta: f.hasta,
    };

    this.cargando.set(true);
    this.historiaService.listar(filtro, this.tabla.parametros()).subscribe({
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

  /**
   * Se ejecuta cuando el backend confirma que la historia se guardó.
   *
   * @param historia historia creada
   */
  alGuardar(historia: Historia): void {
    this.formularioAbierto.set(false);
    this.mensaje.set({ tipo: 'exito', texto: `Historia clínica #${historia.id} registrada correctamente.` });
    this.tabla.reiniciarPagina();
    this.cargar();
  }

  /**
   * Cierra una historia clínica (queda de solo lectura), previa confirmación.
   *
   * @param historia historia abierta
   */
  cerrar(historia: Historia): void {
    const aviso =
      `¿Cerrar la historia de ${historia.mascotaNombre}? Una historia cerrada queda en solo lectura: ` +
      'no admite nuevas anotaciones ni fórmulas.';
    if (!window.confirm(aviso)) {
      return;
    }
    this.historiaService.cerrar(historia.id).subscribe({
      next: () => {
        this.mensaje.set({ tipo: 'exito', texto: `Historia clínica #${historia.id} cerrada.` });
        this.cargar();
      },
      error: (error) => this.mensaje.set({ tipo: 'error', texto: mensajeDeError(error) }),
    });
  }
}
