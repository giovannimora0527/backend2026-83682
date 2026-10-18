import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';
import { Medicamento, Veterinario } from '../models/catalogos.model';

/**
 * Consumo de los catálogos que alimentan los combos de los formularios.
 */
@Injectable({ providedIn: 'root' })
export class CatalogoService {
  /** Cliente HTTP de Angular. */
  private readonly http = inject(HttpClient);

  /**
   * Lista los veterinarios activos (GET /api/veterinarios).
   *
   * @returns veterinarios activos ordenados por nombre
   */
  listarVeterinarios(): Observable<Veterinario[]> {
    return this.http.get<Veterinario[]>(`${API_URL}/veterinarios`);
  }

  /**
   * Lista los medicamentos vigentes, es decir, no vencidos (GET /api/medicamentos).
   *
   * @returns medicamentos vigentes ordenados por nombre
   */
  listarMedicamentos(): Observable<Medicamento[]> {
    return this.http.get<Medicamento[]>(`${API_URL}/medicamentos`);
  }
}
