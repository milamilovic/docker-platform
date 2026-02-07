import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Tag } from '../models/tag.model';
import { SpringPage } from '../models/spring-page.model';
import { env } from '../../../shared/env';

@Injectable({
  providedIn: 'root'
})
export class TagService {
  
  private readonly API_URL = `${env.apiUrl}`;

  constructor(private http: HttpClient) { }

  getTagsByRepository(
    repositoryId: string,
    page: number = 0,
    size: number = 10,
    sort?: string,
    search?: string
  ): Observable<SpringPage<Tag>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (sort) {
      params = params.set('sort', sort);
    }

    if (search) {
      params = params.set('search', search);
    }

    return this.http.get<SpringPage<Tag>>(`${this.API_URL}/tags/${repositoryId}/tags`, { params });
  }

  deleteTag(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/tags/${id}`);
  }
}