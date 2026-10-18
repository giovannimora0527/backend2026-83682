import { NgTemplateOutlet } from '@angular/common';
import { Component, TemplateRef, computed, input, output } from '@angular/core';
import { ControlTabla } from '../../core/utils/control-tabla';

/**
 * Definición de una columna del DataTable.
 */
export interface Columna<T> {
  /** Identificador único de la columna (se usa para el seguimiento de filas). */
  id: string;
  /** Título que se muestra en el encabezado. */
  titulo: string;
  /** Función que obtiene el texto de la celda a partir de la fila. */
  valor: (fila: T) => string;
  /** Nombre lógico de la columna en el backend; si se define, la columna es ordenable. */
  claveOrden?: string;
  /** Función opcional que devuelve la clase CSS de una insignia de color para la celda. */
  insignia?: (fila: T) => string;
}

/**
 * DataTable reutilizable: muestra filas, permite ordenar por columna, paginar y cambiar el tamaño de
 * página. No consulta datos por sí mismo: modifica el {@link ControlTabla} y avisa con el evento
 * {@code cambio} para que la pantalla vuelva a consultar el backend.
 *
 * Las acciones por fila (Ver, Editar...) se entregan como una plantilla (ng-template).
 */
@Component({
  selector: 'app-tabla-datos',
  imports: [NgTemplateOutlet],
  templateUrl: './tabla-datos.html',
})
export class TablaDatos<T> {
  /** Columnas de la tabla. */
  readonly columnas = input.required<Columna<T>[]>();

  /** Filas de la página actual. */
  readonly filas = input<T[]>([]);

  /** Total de registros que cumplen el filtro (todas las páginas). */
  readonly total = input(0);

  /** Estado de paginación y ordenamiento compartido con la pantalla. */
  readonly control = input.required<ControlTabla>();

  /** Si es true, muestra el indicador de carga. */
  readonly cargando = input(false);

  /** Plantilla con las acciones de cada fila (opcional). */
  readonly acciones = input<TemplateRef<unknown> | null>(null);

  /** Texto cuando no hay registros. */
  readonly mensajeVacio = input('No hay registros para mostrar.');

  /** Tamaños de página que ofrece el selector. */
  readonly opcionesTamano = input<number[]>([5, 10, 20, 50]);

  /** Se emite cuando cambia la página, el tamaño o el orden y hay que volver a consultar. */
  readonly cambio = output<void>();

  /** Total de páginas calculado a partir del total de registros. */
  readonly totalPaginas = computed(() => Math.max(1, Math.ceil(this.total() / this.control().tamano())));

  /** Texto "Mostrando X–Y de Z". */
  readonly resumen = computed(() => {
    const total = this.total();
    if (total === 0) {
      return 'Sin registros';
    }
    const tamano = this.control().tamano();
    const desde = this.control().pagina() * tamano + 1;
    const hasta = Math.min(total, (this.control().pagina() + 1) * tamano);
    return `Mostrando ${desde}–${hasta} de ${total}`;
  });

  /**
   * Número de columnas totales (incluye la de acciones), para el mensaje de tabla vacía.
   *
   * @returns cantidad de columnas visibles
   */
  totalColumnas(): number {
    return this.columnas().length + (this.acciones() ? 1 : 0);
  }

  /**
   * Ordena por la columna indicada y avisa a la pantalla.
   *
   * @param columna columna sobre la que el usuario hizo clic
   */
  ordenar(columna: Columna<T>): void {
    if (!columna.claveOrden) {
      return;
    }
    this.control().ordenarPor(columna.claveOrden);
    this.cambio.emit();
  }

  /**
   * Indicador visual del orden de una columna.
   *
   * @param columna columna a evaluar
   * @returns ▲ ascendente, ▼ descendente o ↕ si la tabla no está ordenada por esta columna
   */
  flecha(columna: Columna<T>): string {
    if (this.control().orden() !== columna.claveOrden) {
      return '↕';
    }
    return this.control().direccion() === 'asc' ? '▲' : '▼';
  }

  /**
   * Valor del atributo aria-sort del encabezado (accesibilidad).
   *
   * @param columna columna a evaluar
   * @returns ascending, descending, none, o null si la columna no es ordenable
   */
  ariaOrden(columna: Columna<T>): string | null {
    if (!columna.claveOrden) {
      return null;
    }
    if (this.control().orden() !== columna.claveOrden) {
      return 'none';
    }
    return this.control().direccion() === 'asc' ? 'ascending' : 'descending';
  }

  /**
   * Cambia de página si el número es válido y avisa a la pantalla.
   *
   * @param numero página destino (base 0)
   */
  irAPagina(numero: number): void {
    if (numero < 0 || numero >= this.totalPaginas()) {
      return;
    }
    this.control().irAPagina(numero);
    this.cambio.emit();
  }

  /**
   * Cambia el tamaño de página desde el selector y avisa a la pantalla.
   *
   * @param evento evento change del select
   */
  cambiarTamano(evento: Event): void {
    const tamano = Number((evento.target as HTMLSelectElement).value);
    this.control().cambiarTamano(tamano);
    this.cambio.emit();
  }
}
