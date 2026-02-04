import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Repository, RepositoryDto, RepositoryUpdateDto } from '../models/repository.model';
import { env } from '../../../shared/env';

@Injectable({
  providedIn: 'root'
})
export class RepositoryService {
  
  private readonly API_URL = `${env.apiUrl}/repositories`;

  constructor(private http: HttpClient) { }

  getMyRepositories(): Observable<Repository[]> {
    return this.http.get<Repository[]>(`${this.API_URL}`);
  }

  getOfficialRepositories(): Observable<Repository[]> {
    return this.http.get<Repository[]>(`${this.API_URL}/official`);
  }

  getMyOfficialRepositories(): Observable<Repository[]> {
    return this.http.get<Repository[]>(`${this.API_URL}/my-official`);
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