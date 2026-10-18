import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';
import {
  FormulaActualizarRequest,
  FormulaDetalle,
  FormulaFiltro,
  FormulaRequest,
  FormulaResumen,
} from '../models/formula.model';
import { PageResponse, ParametrosTabla } from '../models/page.model';
import { construirParams } from '../utils/params';

/**
 * Consumo de los servicios REST de fórmulas médicas.
 * No existe una operación de eliminación: las fórmulas se inactivan.
 */
@Injectable({ providedIn: 'root' })
export class FormulaService {
  /** Cliente HTTP de Angular. */
  private readonly http = inject(HttpClient);

  /** URL base del recurso. */
  private readonly url = `${API_URL}/formulas`;

  /**
   * Lista fórmulas en vista resumida (GET /api/formulas).
   *
   * @param filtro criterios de filtrado
   * @param tabla paginación y ordenamiento
   * @returns página de fórmulas resumidas
   */
  listar(filtro: FormulaFiltro, tabla: ParametrosTabla): Observable<PageResponse<FormulaResumen>> {
    return this.http.get<PageResponse<FormulaResumen>>(this.url, { params: construirParams({ ...filtro, ...tabla }) });
  }

  /**
   * Consulta el detalle completo de una fórmula (GET /api/formulas/{id}).
   *
   * @param id identificador de la fórmula
   * @returns detalle de la fórmula
   */
  obtener(id: number): Observable<FormulaDetalle> {
    return this.http.get<FormulaDetalle>(`${this.url}/${id}`);
  }

  /**
   * Registra una fórmula (POST /api/formulas).
   *
   * @param solicitud datos de la fórmula
   * @returns detalle de la fórmula creada
   */
  crear(solicitud: FormulaRequest): Observable<FormulaDetalle> {
    return this.http.post<FormulaDetalle>(this.url, solicitud);
  }

  /**
   * Modifica los datos permitidos de una fórmula (PUT /api/formulas/{id}).
   *
   * @param id identificador de la fórmula
   * @param solicitud nuevos datos
   * @returns detalle de la fórmula actualizada
   */
  actualizar(id: number, solicitud: FormulaActualizarRequest): Observable<FormulaDetalle> {
    return this.http.put<FormulaDetalle>(`${this.url}/${id}`, solicitud);
  }

  /**
   * Inactiva una fórmula en lugar de eliminarla (PATCH /api/formulas/{id}/inactivar).
   *
   * @param id identificador de la fórmula
   * @returns detalle de la fórmula inactiva
   */
  inactivar(id: number): Observable<FormulaDetalle> {
    return this.http.patch<FormulaDetalle>(`${this.url}/${id}/inactivar`, {});
  }

  /**
   * Vuelve a activar una fórmula inactiva (PATCH /api/formulas/{id}/activar).
   *
   * @param id identificador de la fórmula
   * @returns detalle de la fórmula activa
   */
  activar(id: number): Observable<FormulaDetalle> {
    return this.http.patch<FormulaDetalle>(`${this.url}/${id}/activar`, {});
  }
}
