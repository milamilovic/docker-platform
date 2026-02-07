import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {Hello} from './features/hello/hello';
import {AuthGuard} from './features/auth/auth.guard';
import { Home } from './features/home/home';
import { SearchResults } from './features/search-results/search-results';

const routes: Routes = [
  { path: '', component: Home},
  { path: 'search', component: SearchResults},
  { path: 'hello', component: Hello, canActivate: [AuthGuard], data: { role: ['REGULAR']} },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
