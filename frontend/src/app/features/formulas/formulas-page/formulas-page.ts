import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { FormulaDetalle, FormulaFiltro, FormulaResumen } from '../../../core/models/formula.model';
import { Mensaje } from '../../../core/models/mensaje.model';
import { FormulaService } from '../../../core/services/formula.service';
import { ControlTabla } from '../../../core/utils/control-tabla';
import { formatearFecha, formatearFechaHora } from '../../../core/utils/fechas';
import { mensajeDeError } from '../../../core/utils/mensaje-error';
import { Alerta } from '../../../shared/alerta/alerta';
import { Modal } from '../../../shared/modal/modal';
import { Columna, TablaDatos } from '../../../shared/tabla-datos/tabla-datos';
import { FormulaDetalleVista } from '../formula-detalle/formula-detalle';
import { FormulaForm } from '../formula-form/formula-form';

/**
 * Pantalla de gestión de fórmulas médicas: DataTable en vista RESUMIDA con filtros, ordenamiento y
 * paginación (inicialmente por fecha de creación descendente: las más recientes primero).
 * «Ver» abre el detalle completo y «Editar» el formulario con los datos modificables.
 * Las fórmulas nunca se eliminan: se inactivan (y se pueden volver a activar).
 */
@Component({
  selector: 'app-formulas-page',
  imports: [ReactiveFormsModule, TablaDatos, Modal, Alerta, FormulaForm, FormulaDetalleVista],
  templateUrl: './formulas-page.html',
})
export class FormulasPage implements OnInit {
  /** Constructor de formularios reactivos. */
  private readonly fb = inject(FormBuilder);

  /** Servicio de fórmulas. */
  private readonly formulaService = inject(FormulaService);

  /** Orden inicial exigido por el enunciado: fecha de creación, descendente. */
  readonly tabla = new ControlTabla('fechaCreacion', 'desc');

  /** Filas de la página actual. */
  readonly filas = signal<FormulaResumen[]>([]);

  /** Total de fórmulas que cumplen el filtro. */
  readonly total = signal(0);

  /** Indica que se está consultando el backend. */
  readonly cargando = signal(false);

  /** Mensaje de éxito o error de la pantalla. */
  readonly mensaje = signal<Mensaje | null>(null);

  /** Si el formulario modal está abierto. */
  readonly formularioAbierto = signal(false);

  /** Fórmula que se está editando (null = fórmula nueva). */
  readonly formulaEnEdicion = signal<FormulaDetalle | null>(null);

  /** Fórmula cuyo detalle se está viendo (null = ninguna). */
  readonly formulaVista = signal<FormulaDetalle | null>(null);

  /** Formulario de filtros. */
  readonly filtroForm = this.fb.group({
    mascota: this.fb.nonNullable.control(''),
    cliente: this.fb.nonNullable.control(''),
    medicamento: this.fb.nonNullable.control(''),
    activo: this.fb.nonNullable.control<'true' | 'false' | ''>(''),
    desde: this.fb.nonNullable.control(''),
    hasta: this.fb.nonNullable.control(''),
  });

  /** Columnas del DataTable: solo información resumida (el resto está en «Ver»). */
  readonly columnas: Columna<FormulaResumen>[] = [
    { id: 'creada', titulo: 'Creada', claveOrden: 'fechaCreacion', valor: (f) => formatearFechaHora(f.fechaCreacion) },
    { id: 'emision', titulo: 'Emisión', claveOrden: 'fechaEmision', valor: (f) => formatearFecha(f.fechaEmision) },
    { id: 'mascota', titulo: 'Mascota', claveOrden: 'mascota', valor: (f) => f.mascotaNombre },
    { id: 'cliente', titulo: 'Cliente', claveOrden: 'cliente', valor: (f) => f.clienteNombre },
    { id: 'medicamento', titulo: 'Medicamento', claveOrden: 'medicamento', valor: (f) => f.medicamentoNombre },
    { id: 'posologia', titulo: 'Posología', valor: (f) => f.posologia },
    {
      id: 'estado',
      titulo: 'Estado',
      claveOrden: 'activo',
      valor: (f) => (f.activo ? 'Activa' : 'Inactiva'),
      insignia: (f) => (f.activo ? 'badge-verde' : 'badge-gris'),
    },
  ];

  /** Carga el listado inicial. */
  ngOnInit(): void {
    this.cargar();
  }

  /** Consulta al backend la página actual con los filtros, el orden y la paginación vigentes. */
  cargar(): void {
    const f = this.filtroForm.getRawValue();
    const filtro: FormulaFiltro = {
      mascota: f.mascota.trim(),
      cliente: f.cliente.trim(),
      medicamento: f.medicamento.trim(),
      activo: f.activo,
      desde: f.desde,
      hasta: f.hasta,
    };

    this.cargando.set(true);
    this.formulaService.listar(filtro, this.tabla.parametros()).subscribe({
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

  /** Abre el formulario para registrar una fórmula nueva. */
  nueva(): void {
    this.formulaEnEdicion.set(null);
    this.formularioAbierto.set(true);
  }

  /**
   * Consulta el detalle completo de una fórmula y lo muestra en una ventana modal.
   *
   * @param fila fórmula de la fila (vista resumida)
   */
  ver(fila: FormulaResumen): void {
    this.formulaService.obtener(fila.id).subscribe({
      next: (detalle) => this.formulaVista.set(detalle),
      error: (error) => this.mensaje.set({ tipo: 'error', texto: mensajeDeError(error) }),
    });
  }

  /**
   * Consulta el detalle de una fórmula activa y abre el formulario de edición.
   *
   * @param fila fórmula de la fila (vista resumida)
   */
  editar(fila: FormulaResumen): void {
    this.formulaService.obtener(fila.id).subscribe({
      next: (detalle) => {
        this.formulaEnEdicion.set(detalle);
        this.formularioAbierto.set(true);
      },
      error: (error) => this.mensaje.set({ tipo: 'error', texto: mensajeDeError(error) }),
    });
  }

  /** Cierra el formulario sin guardar. */
  cerrarFormulario(): void {
    this.formularioAbierto.set(false);
    this.formulaEnEdicion.set(null);
  }

  /**
   * Se ejecuta cuando el backend confirma que la fórmula se guardó.
   *
   * @param formula fórmula guardada
   */
  alGuardar(formula: FormulaDetalle): void {
    const esNueva = this.formulaEnEdicion() === null;
    this.cerrarFormulario();
    this.mensaje.set({
      tipo: 'exito',
      texto: esNueva ? `Fórmula #${formula.id} registrada correctamente.` : `Fórmula #${formula.id} actualizada correctamente.`,
    });
    if (esNueva) {
      this.tabla.reiniciarPagina();
    }
    this.cargar();
  }

  /**
   * Inactiva o activa una fórmula (nunca se elimina), previa confirmación.
   *
   * @param fila fórmula de la fila
   */
  cambiarEstado(fila: FormulaResumen): void {
    const aviso = fila.activo
      ? '¿Inactivar esta fórmula? No se elimina: queda registrada como inactiva y se puede volver a activar.'
      : '¿Volver a activar esta fórmula?';
    if (!window.confirm(aviso)) {
      return;
    }
    const peticion = fila.activo ? this.formulaService.inactivar(fila.id) : this.formulaService.activar(fila.id);
    peticion.subscribe({
      next: () => {
        this.mensaje.set({
          tipo: 'exito',
          texto: fila.activo ? `Fórmula #${fila.id} inactivada.` : `Fórmula #${fila.id} activada.`,
        });
        this.cargar();
      },
      error: (error) => this.mensaje.set({ tipo: 'error', texto: mensajeDeError(error) }),
    });
  }
}
