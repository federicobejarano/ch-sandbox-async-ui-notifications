import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'admin/affiliations',
    pathMatch: 'full',
  },
  {
    path: 'admin/affiliations',
    loadComponent: () =>
      import('./pages/admin-affiliations/admin-affiliations.page').then(
        m => m.AdminAffiliationsPage,
      ),
  },
];
