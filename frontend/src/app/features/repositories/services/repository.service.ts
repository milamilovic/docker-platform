import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Repository, RepositoryDto, RepositoryUpdateDto } from '../models/repository.model';
import { SpringPage } from '../models/spring-page.model';
import { env } from '../../../shared/env';

@Injectable({
  providedIn: 'root'
})
export class RepositoryService {
  
  private readonly API_URL = `${env.apiUrl}/repositories`;

  constructor(private http: HttpClient) { }

  getMyRepositories(
    page: number = 0,
    size: number = 10,
    sort?: string,
    search?: string,
    visibility: string = 'all'
  ): Observable<SpringPage<Repository>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('visibility', visibility);

    if (sort) {
      params = params.set('sort', sort);
    }

    if (search) {
      params = params.set('search', search);
    }

    return this.http.get<SpringPage<Repository>>(`${this.API_URL}`, { params });
  }

  getOfficialRepositories(
    page: number = 0,
    size: number = 10,
    sort?: string,
    search?: string
  ): Observable<SpringPage<Repository>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (sort) {
      params = params.set('sort', sort);
    }

    if (search) {
      params = params.set('search', search);
    }

    return this.http.get<SpringPage<Repository>>(`${this.API_URL}/official`, { params });
  }

  getMyOfficialRepositories(
    page: number = 0,
    size: number = 10,
    sort?: string,
    search?: string
  ): Observable<SpringPage<Repository>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (sort) {
      params = params.set('sort', sort);
    }

    if (search) {
      params = params.set('search', search);
    }

    return this.http.get<SpringPage<Repository>>(`${this.API_URL}/my-official`, { params });
  }

  getRepositoryById(id: string): Observable<Repository> {
    return this.http.get<Repository>(`${this.API_URL}/${id}`);
  }

  createRepository(dto: RepositoryDto): Observable<Repository> {
    return this.http.post<Repository>(`${this.API_URL}`, dto);
  }

  updateRepository(id: string, dto: RepositoryUpdateDto): Observable<Repository> {
    return this.http.put<Repository>(`${this.API_URL}/${id}`, dto);
  }

  deleteRepository(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}