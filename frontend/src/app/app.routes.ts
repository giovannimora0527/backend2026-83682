import { Routes } from '@angular/router';

/**
 * Rutas de la aplicación. Cada módulo de gestión se carga de forma diferida (lazy loading).
 */
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'citas' },
  {
    path: 'citas',
    title: 'Citas médicas',
    loadComponent: () => import('./features/citas/citas-page/citas-page').then((m) => m.CitasPage),
  },
  {
    path: 'historias',
    title: 'Historias clínicas',
    loadComponent: () => import('./features/historias/historias-page/historias-page').then((m) => m.HistoriasPage),
  },
  {
    path: 'formulas',
    title: 'Fórmulas médicas',
    loadComponent: () => import('./features/formulas/formulas-page/formulas-page').then((m) => m.FormulasPage),
  },
  { path: '**', redirectTo: 'citas' },
];
