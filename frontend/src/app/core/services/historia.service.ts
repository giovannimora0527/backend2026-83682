import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';
import {
  Anotacion,
  AnotacionFiltro,
  AnotacionRequest,
  Historia,
  HistoriaFiltro,
  HistoriaRequest,
} from '../models/historia.model';
import { PageResponse, ParametrosTabla } from '../models/page.model';
import { construirParams } from '../utils/params';

/**
 * Consumo de los servicios REST de historias clínicas y de sus anotaciones.
 */
@Injectable({ providedIn: 'root' })
export class HistoriaService {
  /** Cliente HTTP de Angular. */
  private readonly http = inject(HttpClient);

  /** URL base del recurso. */
  private readonly url = `${API_URL}/historias`;

  /**
   * Lista historias clínicas con filtros, paginación y ordenamiento (GET /api/historias).
   *
   * @param filtro criterios de filtrado
   * @param tabla paginación y ordenamiento
   * @returns página de historias
   */
  listar(filtro: HistoriaFiltro, tabla: ParametrosTabla): Observable<PageResponse<Historia>> {
    return this.http.get<PageResponse<Historia>>(this.url, { params: construirParams({ ...filtro, ...tabla }) });
  }

  /**
   * Consulta una historia clínica (GET /api/historias/{id}).
   *
   * @param id identificador de la historia
   * @returns la historia
   */
  obtener(id: number): Observable<Historia> {
    return this.http.get<Historia>(`${this.url}/${id}`);
  }

  /**
   * Registra una historia clínica (POST /api/historias).
   *
   * @param solicitud datos de la historia
   * @returns la historia creada
   */
  crear(solicitud: HistoriaRequest): Observable<Historia> {
    return this.http.post<Historia>(this.url, solicitud);
  }

  /**
   * Cierra una historia clínica: queda de solo lectura (PATCH /api/historias/{id}/cerrar).
   *
   * @param id identificador de la historia
   * @returns la historia cerrada
   */
  cerrar(id: number): Observable<Historia> {
    return this.http.patch<Historia>(`${this.url}/${id}/cerrar`, {});
  }

  /**
   * Lista las anotaciones de una historia (GET /api/historias/{id}/anotaciones).
   *
   * @param historiaId identificador de la historia
   * @param filtro criterios de filtrado
   * @param tabla paginación y ordenamiento
   * @returns página de anotaciones (por defecto, de la más reciente a la más antigua)
   */
  listarAnotaciones(
    historiaId: number,
    filtro: AnotacionFiltro,
    tabla: ParametrosTabla,
  ): Observable<PageResponse<Anotacion>> {
    return this.http.get<PageResponse<Anotacion>>(`${this.url}/${historiaId}/anotaciones`, {
      params: construirParams({ ...filtro, ...tabla }),
    });
  }

  /**
   * Consulta el detalle de una anotación (GET /api/historias/{id}/anotaciones/{anotacionId}).
   *
   * @param historiaId identificador de la historia
   * @param anotacionId identificador de la anotación
   * @returns la anotación
   */
  obtenerAnotacion(historiaId: number, anotacionId: number): Observable<Anotacion> {
    return this.http.get<Anotacion>(`${this.url}/${historiaId}/anotaciones/${anotacionId}`);
  }

  /**
   * Agrega una anotación a una historia abierta (POST /api/historias/{id}/anotaciones).
   *
   * @param historiaId identificador de la historia
   * @param solicitud datos de la anotación
   * @returns la anotación creada
   */
  crearAnotacion(historiaId: number, solicitud: AnotacionRequest): Observable<Anotacion> {
    return this.http.post<Anotacion>(`${this.url}/${historiaId}/anotaciones`, solicitud);
  }
}
