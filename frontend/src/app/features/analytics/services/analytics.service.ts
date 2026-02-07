import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, delay } from 'rxjs';
import { LogEntry, LogSearchQuery, LogSearchResponse} from '../models/log.model';
import { env } from '../../../shared/env';

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {

  private readonly API_URL = `${env.apiUrl}/analytics/logs`;

  constructor(private http: HttpClient) { }

  searchLogs(query: LogSearchQuery): Observable<LogSearchResponse> {
    let params = new HttpParams()
      .set('size', query.size?.toString() || '50')
      .set('from', query.from?.toString() || '0');

    if (query.query) params = params.set('query', query.query);
    if (query.startDate) params = params.set('startDate', query.startDate);
    if (query.endDate) params = params.set('endDate', query.endDate);
    if (query.levels && query.levels.length > 0) {
      params = params.set('levels', query.levels.join(','));
    }

    return this.http.get<LogSearchResponse>(this.API_URL, { params });
  }

  exportLogs(query: LogSearchQuery): Observable<Blob> {
    return this.http.post(`${this.API_URL}/export`, query, { responseType: 'blob' });
  }
}
