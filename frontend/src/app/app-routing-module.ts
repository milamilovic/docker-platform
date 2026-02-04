import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {Hello} from './features/hello/hello';
import {AuthGuard} from './features/auth/auth.guard';

import { RepositoriesList } from './features/repositories/repositories-list/repositories-list';
import { RepositoryDetails } from './features/repositories/repository-details/repository-details';
import { OfficialRepositories } from './features/repositories/official-repositories/official-repositories';
import { Home } from './features/home/home';


const routes: Routes = [
  { path: '', component: Home},
  { path: 'hello', component: Hello, canActivate: [AuthGuard], data: { role: ['REGULAR']} },
  { path: 'repositories', component: RepositoriesList, canActivate: [AuthGuard], data: { role: ['REGULAR', 'ADMIN'] } },
  { path: 'repositories/:id', component: RepositoryDetails, canActivate: [AuthGuard], data: { role: ['REGULAR', 'ADMIN'] } },
  { path: 'admin/official-repositories', component: OfficialRepositories, canActivate: [AuthGuard], data: { role: ['ADMIN'] } },
  { path: '', redirectTo: '', pathMatch: 'full' },
  { path: '**', redirectTo: '', pathMatch: 'full' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
