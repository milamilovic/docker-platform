import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Tag } from '../models/tag.model';
import { env } from '../../../shared/env';

@Injectable({
  providedIn: 'root'
})
export class TagService {
  
  private readonly API_URL = `${env.apiUrl}`;

  constructor(private http: HttpClient) { }

  getTagsByRepository(repositoryId: string): Observable<Tag[]> {
    return this.http.get<Tag[]>(`${this.API_URL}/tags/${repositoryId}/tags`);
  }

  deleteTag(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/tags/${id}`);
  }
}