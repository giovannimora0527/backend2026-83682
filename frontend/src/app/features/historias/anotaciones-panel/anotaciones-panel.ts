import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Veterinario } from '../../../core/models/catalogos.model';
import { Anotacion, AnotacionFiltro, Historia } from '../../../core/models/historia.model';
import { Mensaje } from '../../../core/models/mensaje.model';
import { CatalogoService } from '../../../core/services/catalogo.service';
import { HistoriaService } from '../../../core/services/historia.service';
import { ControlTabla } from '../../../core/utils/control-tabla';
import { formatearFechaHora } from '../../../core/utils/fechas';
import { mensajeDeError } from '../../../core/utils/mensaje-error';
import { Alerta } from '../../../shared/alerta/alerta';
import { Columna, TablaDatos } from '../../../shared/tabla-datos/tabla-datos';
import { AnotacionForm } from '../anotacion-form/anotacion-form';

/**
 * Histórico de anotaciones de una historia clínica: DataTable de las anotaciones de la más reciente a la
 * más antigua, con filtros, ordenamiento por fecha, paginación, consulta del detalle de cada anotación
 * y formulario para agregar nuevas anotaciones (solo si la historia está abierta).
 */
@Component({
  selector: 'app-anotaciones-panel',
  imports: [ReactiveFormsModule, TablaDatos, Alerta, AnotacionForm],
  templateUrl: './anotaciones-panel.html',
})
export class AnotacionesPanel implements OnInit {
  /** Constructor de formularios reactivos. */
  private readonly fb = inject(FormBuilder);

  /** Servicio de historias clínicas y anotaciones. */
  private readonly historiaService = inject(HistoriaService);

  /** Servicio de catálogos (veterinarios). */
  private readonly catalogoService = inject(CatalogoService);

  /** Historia clínica cuyo histórico se muestra. */
  readonly historia = input.required<Historia>();

  /** Orden inicial exigido por el enunciado: de la más reciente a la más antigua. */
  readonly tabla = new ControlTabla('fechaHora', 'desc', 5);

  /** Veterinarios disponibles para el filtro y el formulario. */
  readonly veterinarios = signal<Veterinario[]>([]);

  /** Anotaciones de la página actual. */
  readonly filas = signal<Anotacion[]>([]);

  /** Total de anotaciones que cumplen el filtro. */
  readonly total = signal(0);

  /** Indica que se está consultando el backend. */
  readonly cargando = signal(false);

  /** Mensaje de éxito o error del panel. */
  readonly mensaje = signal<Mensaje | null>(null);

  /** Anotación cuyo detalle se está mostrando (null = ninguna). */
  readonly seleccionada = signal<Anotacion | null>(null);

  /** Si el formulario de nueva anotación está abierto. */
  readonly formularioAbierto = signal(false);

  /** Formulario de filtros. */
  readonly filtroForm = this.fb.group({
    texto: this.fb.nonNullable.control(''),
    veterinarioId: this.fb.control<number | null>(null),
    desde: this.fb.nonNullable.control(''),
    hasta: this.fb.nonNullable.control(''),
  });

  /** Columnas del DataTable de anotaciones. */
  readonly columnas: Columna<Anotacion>[] = [
    { id: 'fecha', titulo: 'Fecha y hora', claveOrden: 'fechaHora', valor: (a) => formatearFechaHora(a.fechaHora) },
    { id: 'veterinario', titulo: 'Veterinario', claveOrden: 'veterinario', valor: (a) => a.veterinarioNombre },
    { id: 'descripcion', titulo: 'Descripción', valor: (a) => this.resumir(a.descripcion) },
  ];

  /** Carga las anotaciones y el catálogo de veterinarios. */
  ngOnInit(): void {
    this.cargar();
    this.catalogoService.listarVeterinarios().subscribe({
      next: (veterinarios) => this.veterinarios.set(veterinarios),
      error: (error) => this.mensaje.set({ tipo: 'error', texto: mensajeDeError(error) }),
    });
  }

  /**
   * Acorta un texto largo para mostrarlo en la tabla (el texto completo está en el detalle).
   *
   * @param texto texto original
   * @returns texto de máximo 70 caracteres
   */
  resumir(texto: string): string {
    return texto.length > 70 ? `${texto.slice(0, 70)}…` : texto;
  }

  /**
   * Indica si se pueden agregar anotaciones (la historia debe estar abierta).
   *
   * @returns true si la historia está abierta
   */
  puedeAnotar(): boolean {
    return this.historia().estado === 'ABIERTA';
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

  /** Consulta al backend la página actual con los filtros, el orden y la paginación vigentes. */
  cargar(): void {
    const f = this.filtroForm.getRawValue();
    const filtro: AnotacionFiltro = {
      texto: f.texto.trim(),
      veterinarioId: f.veterinarioId,
      desde: f.desde,
      hasta: f.hasta,
    };

    this.cargando.set(true);
    this.historiaService.listarAnotaciones(this.historia().id, filtro, this.tabla.parametros()).subscribe({
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
   * Consulta el detalle de una anotación y lo muestra debajo de la tabla.
   *
   * @param anotacion anotación de la fila
   */
  verDetalle(anotacion: Anotacion): void {
    this.historiaService.obtenerAnotacion(this.historia().id, anotacion.id).subscribe({
      next: (detalle) => this.seleccionada.set(detalle),
      error: (error) => this.mensaje.set({ tipo: 'error', texto: mensajeDeError(error) }),
    });
  }

  /**
   * Se ejecuta cuando el backend confirma que la anotación se guardó: vuelve a la primera página
   * (la nueva anotación aparece de primera por el orden descendente).
   */
  alGuardar(): void {
    this.formularioAbierto.set(false);
    this.mensaje.set({ tipo: 'exito', texto: 'Anotación registrada correctamente.' });
    this.tabla.reiniciarPagina();
    this.cargar();
  }
}
