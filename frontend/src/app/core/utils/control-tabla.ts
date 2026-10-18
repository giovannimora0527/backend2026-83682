import { WritableSignal, signal } from '@angular/core';
import { DireccionOrden, ParametrosTabla } from '../models/page.model';

/**
 * Estado de paginación y ordenamiento de un DataTable.
 * Cada pantalla crea una instancia y se la entrega al componente de tabla, que la modifica cuando el
 * usuario cambia de página, de tamaño o de columna de orden. Así se evita repetir esta lógica.
 */
export class ControlTabla {
  /** Página actual (base 0). */
  readonly pagina = signal(0);

  /** Registros por página. */
  readonly tamano: WritableSignal<number>;

  /** Nombre lógico de la columna por la que se ordena. */
  readonly orden: WritableSignal<string>;

  /** Dirección del ordenamiento. */
  readonly direccion: WritableSignal<DireccionOrden>;

  /**
   * Crea el control con el ordenamiento inicial que pide el enunciado para cada listado.
   *
   * @param ordenInicial columna por la que se ordena al abrir la pantalla
   * @param direccionInicial dirección inicial (por defecto descendente: lo más reciente primero)
   * @param tamanoInicial registros por página iniciales
   */
  constructor(ordenInicial: string, direccionInicial: DireccionOrden = 'desc', tamanoInicial = 10) {
    this.orden = signal(ordenInicial);
    this.direccion = signal<DireccionOrden>(direccionInicial);
    this.tamano = signal(tamanoInicial);
  }

  /**
   * Ordena por la columna indicada: si ya estaba ordenada invierte la dirección, si no, ordena ascendente.
   * Siempre vuelve a la primera página.
   *
   * @param clave nombre lógico de la columna
   */
  ordenarPor(clave: string): void {
    if (this.orden() === clave) {
      this.direccion.set(this.direccion() === 'asc' ? 'desc' : 'asc');
    } else {
      this.orden.set(clave);
      this.direccion.set('asc');
    }
    this.pagina.set(0);
  }

  /**
   * Cambia de página.
   *
   * @param numero número de página (base 0)
   */
  irAPagina(numero: number): void {
    this.pagina.set(Math.max(0, numero));
  }

  /**
   * Cambia el número de registros por página y vuelve a la primera página.
   *
   * @param tamano nuevo tamaño de página
   */
  cambiarTamano(tamano: number): void {
    this.tamano.set(tamano);
    this.pagina.set(0);
  }

  /** Vuelve a la primera página (se usa cuando cambian los filtros). */
  reiniciarPagina(): void {
    this.pagina.set(0);
  }

  /**
   * Devuelve el estado actual como parámetros para la petición al backend.
   *
   * @returns parámetros de paginación y ordenamiento
   */
  parametros(): ParametrosTabla {
    return {
      pagina: this.pagina(),
      tamano: this.tamano(),
      orden: this.orden(),
      direccion: this.direccion(),
    };
  }
}
