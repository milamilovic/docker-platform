import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {Hello} from './features/hello/hello';
import {AuthGuard} from './features/auth/auth.guard';
import { Analytics } from './features/analytics/analytics/analytics';
import { Home } from './features/home/home';

const routes: Routes = [
  { path: '', component: Home},
  { path: 'hello', component: Hello, canActivate: [AuthGuard], data: { role: ['REGULAR']} },
  { path: 'analytics', component: Analytics, canActivate: [AuthGuard], data: { role: ['ADMIN'] } },
  { path: '', redirectTo: '', pathMatch: 'full' },
  { path: '**', redirectTo: '', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
