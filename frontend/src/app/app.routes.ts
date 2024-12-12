import { Routes } from '@angular/router';
import { ExemploComponent } from './modules/exemplo/exemplo.component';

export const routes: Routes = [
  { path: 'exemplo', component: ExemploComponent },
  { path: '', redirectTo: '/exemplo', pathMatch: 'full' },
];
