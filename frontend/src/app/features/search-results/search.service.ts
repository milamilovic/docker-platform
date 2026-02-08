import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Page } from '../../shared/models/page';
import { Repository } from '../../shared/models/repository';
import { env } from '../../shared/env';

@Injectable({
  providedIn: 'root',
})
export class SearchService {
  constructor(private http: HttpClient) { }

  search(q: string, page: number, size: number): Observable<Page<Repository>> {
    return this.http.get<Page<Repository>>(
      `${env.apiUrl}/public/repositories/search?q=${q}&page=${page}&size=${size}`
    );
  }
}