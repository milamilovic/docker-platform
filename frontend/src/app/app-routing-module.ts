import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {Hello} from './features/hello/hello';
import {AuthGuard} from './features/auth/auth.guard';

const routes: Routes = [
  { path: '', redirectTo: '', pathMatch: 'full' },
  { path: 'hello', component: Hello, canActivate: [AuthGuard], data: { role: ['REGULAR']} },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
