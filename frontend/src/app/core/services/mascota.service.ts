import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';
import { Mascota } from '../models/catalogos.model';

/**
 * Consumo del servicio REST de mascotas.
 */
@Injectable({ providedIn: 'root' })
export class MascotaService {
  /** Cliente HTTP de Angular. */
  private readonly http = inject(HttpClient);

  /** URL base del recurso. */
  private readonly url = `${API_URL}/mascotas`;

  /**
   * Consulta una mascota (GET /api/mascotas/{id}).
   *
   * @param id identificador de la mascota
   * @returns la mascota
   */
  obtener(id: number): Observable<Mascota> {
    return this.http.get<Mascota>(`${this.url}/${id}`);
  }
}
