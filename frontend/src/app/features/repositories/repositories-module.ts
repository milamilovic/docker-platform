import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

import { RepositoriesList } from './repositories-list/repositories-list';
import { RepositoryDetails } from './repository-details/repository-details';
import { OfficialRepositories } from './official-repositories/official-repositories';

import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { SelectModule } from 'primeng/select';
import { PaginatorModule } from 'primeng/paginator';

@NgModule({
  declarations: [
    RepositoriesList,
    RepositoryDetails,
    OfficialRepositories
  ],
  imports: [
    CommonModule,
    ButtonModule,
    InputTextModule,
    IconFieldModule,
    InputIconModule,
    DialogModule,
    ToastModule,
    SelectModule,
    ReactiveFormsModule,
    FormsModule,
    PaginatorModule
  ],
  exports: [
    RepositoriesList,
    RepositoryDetails,
    OfficialRepositories
  ]
})
export class RepositoriesModule { }
