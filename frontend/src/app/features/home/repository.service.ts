import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Page } from '../../shared/models/page';
import { Repository } from '../../shared/models/repository';
import { env } from '../../shared/env';

@Injectable({
  providedIn: 'root',
})
export class RepositoryService {
  constructor(private http: HttpClient) { }

  getTopPulled(page: number, size: number): Observable<Page<Repository>> {
    return this.http.get<Page<Repository>>(
      `${env.apiUrl}/public/repositories/top-pulled?page=${page}&size=${size}`
    );
  }

  getTopStarred(page: number, size: number): Observable<Page<Repository>> {
    return this.http.get<Page<Repository>>(
      `${env.apiUrl}/public/repositories/top-starred?page=${page}&size=${size}`
    );
  }
}
