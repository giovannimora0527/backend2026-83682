import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

/**
 * Componente raíz: barra de navegación con el menú de módulos y el área donde se muestra cada pantalla.
 */
@Component({
  selector: 'app-shell',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <header class="barra">
      <div class="barra-contenido">
        <span class="marca">🐾 Centro Veterinario</span>
        <nav aria-label="Menú principal">
          @for (opcion of opciones; track opcion.ruta) {
            <a [routerLink]="opcion.ruta" routerLinkActive="activo">{{ opcion.titulo }}</a>
          }
        </nav>
      </div>
    </header>
    <main class="contenedor">
      <router-outlet></router-outlet>
    </main>
  `,
})
export class Shell {
  /** Opciones del menú principal: un módulo de gestión por opción. */
  readonly opciones = [
    { ruta: '/citas', titulo: 'Citas médicas' },
    { ruta: '/historias', titulo: 'Historias clínicas' },
    { ruta: '/formulas', titulo: 'Fórmulas médicas' },
  ];
}
