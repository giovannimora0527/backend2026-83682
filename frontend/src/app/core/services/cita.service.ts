import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';
import { Cita, CitaActualizarRequest, CitaFiltro, CitaRequest } from '../models/cita.model';
import { PageResponse, ParametrosTabla } from '../models/page.model';
import { construirParams } from '../utils/params';

/**
 * Consumo de los servicios REST de citas médicas.
 */
@Injectable({ providedIn: 'root' })
export class CitaService {
  /** Cliente HTTP de Angular. */
  private readonly http = inject(HttpClient);

  /** URL base del recurso. */
  private readonly url = `${API_URL}/citas`;

  /**
   * Lista citas con filtros, paginación y ordenamiento (GET /api/citas).
   *
   * @param filtro criterios de filtrado
   * @param tabla paginación y ordenamiento
   * @returns página de citas
   */
  listar(filtro: CitaFiltro, tabla: ParametrosTabla): Observable<PageResponse<Cita>> {
    return this.http.get<PageResponse<Cita>>(this.url, { params: construirParams({ ...filtro, ...tabla }) });
  }

  /**
   * Consulta una cita (GET /api/citas/{id}).
   *
   * @param id identificador de la cita
   * @returns la cita
   */
  obtener(id: number): Observable<Cita> {
    return this.http.get<Cita>(`${this.url}/${id}`);
  }

  /**
   * Crea una cita (POST /api/citas).
   *
   * @param solicitud datos de la cita
   * @returns la cita creada
   */
  crear(solicitud: CitaRequest): Observable<Cita> {
    return this.http.post<Cita>(this.url, solicitud);
  }

  /**
   * Edita una cita (PUT /api/citas/{id}).
   *
   * @param id identificador de la cita
   * @param solicitud nuevos datos
   * @returns la cita actualizada
   */
  actualizar(id: number, solicitud: CitaActualizarRequest): Observable<Cita> {
    return this.http.put<Cita>(`${this.url}/${id}`, solicitud);
  }

  /**
   * Reactiva una cita cancelada, operación explícita (PATCH /api/citas/{id}/reactivar).
   *
   * @param id identificador de la cita
   * @returns la cita reactivada
   */
  reactivar(id: number): Observable<Cita> {
    return this.http.patch<Cita>(`${this.url}/${id}/reactivar`, {});
  }
}
