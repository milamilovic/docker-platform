import { Injectable } from '@angular/core';
import { env } from '../../../shared/env';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SpringPage } from '../models/spring-page.model';
import { Repository } from '../models/repository.model';

@Injectable({
  providedIn: 'root',
})
export class PublicRepositoryService {
  private readonly API_URL = `${env.apiUrl}/public/repositories`;

  constructor(private http: HttpClient) { }

  getOfficialRepositories(
    page: number = 0,
    size: number = 10,
    badge: string, 
    sort?: string,
    search?: string
  ): Observable<SpringPage<Repository>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('badge', badge); 

    if (sort) {
      params = params.set('sort', sort);
    }

    if (search) {
      params = params.set('search', search);
    }

    return this.http.get<SpringPage<Repository>>(`${this.API_URL}/official`, { params });
  }

}
