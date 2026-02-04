import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RepositoriesList } from './repositories-list/repositories-list';
import { RepositoryDetails } from './repository-details/repository-details';
import { OfficialRepositories } from './official-repositories/official-repositories';



@NgModule({
  declarations: [
    RepositoriesList,
    RepositoryDetails,
    OfficialRepositories
  ],
  imports: [
    CommonModule
  ]
})
export class RepositoriesModule { }
