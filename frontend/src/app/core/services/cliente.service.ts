import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';
import { Cliente, Mascota } from '../models/catalogos.model';
import { PageResponse } from '../models/page.model';
import { construirParams } from '../utils/params';

/**
 * Consumo de los servicios REST de clientes y de las mascotas de un cliente.
 */
@Injectable({ providedIn: 'root' })
export class ClienteService {
  /** Cliente HTTP de Angular. */
  private readonly http = inject(HttpClient);

  /** URL base del recurso. */
  private readonly url = `${API_URL}/clientes`;

  /**
   * Lista los clientes activos (GET /api/clientes).
   *
   * @param texto texto a buscar en nombres, apellidos o documento (opcional)
   * @param pagina número de página (base 0)
   * @param tamano registros por página
   * @returns página de clientes
   */
  listar(texto = '', pagina = 0, tamano = 100): Observable<PageResponse<Cliente>> {
    return this.http.get<PageResponse<Cliente>>(this.url, { params: construirParams({ texto, pagina, tamano }) });
  }

  /**
   * Consulta un cliente (GET /api/clientes/{id}).
   *
   * @param id identificador del cliente
   * @returns el cliente
   */
  obtener(id: number): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.url}/${id}`);
  }

  /**
   * Lista únicamente las mascotas del cliente indicado (GET /api/clientes/{id}/mascotas).
   * Alimenta el combo dependiente Cliente → Mascota.
   *
   * @param clienteId identificador del cliente
   * @returns mascotas activas del cliente
   */
  listarMascotas(clienteId: number): Observable<Mascota[]> {
    return this.http.get<Mascota[]>(`${this.url}/${clienteId}/mascotas`);
  }
}
