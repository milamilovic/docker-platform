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

  // mock podaci
  private mockLogs: LogEntry[] = [
    {
      timestamp: '2026-02-04T19:30:15.123Z',
      level: 'ERROR',
      message: 'Failed to connect to database: Connection timeout',
      logger: 'com.dockerplatform.backend.repository.UserRepository',
      thread: 'http-nio-8080-exec-5'
    },
    {
      timestamp: '2026-02-04T14:30:15.123Z',
      level: 'ERROR',
      message: 'Failed to connect to database: Connection timeout',
      logger: 'com.dockerplatform.backend.repository.UserRepository',
      thread: 'http-nio-8080-exec-5'
    },
    {
      timestamp: '2026-02-04T14:28:42.456Z',
      level: 'WARNING',
      message: 'Slow query detected: Query took 2.5 seconds',
      logger: 'com.dockerplatform.backend.service.RepositoryService',
      thread: 'http-nio-8080-exec-3'
    },
    {
      timestamp: '2026-02-04T14:25:10.789Z',
      level: 'INFO',
      message: 'User johndoe logged in successfully',
      logger: 'com.dockerplatform.backend.security.AuthController',
      thread: 'http-nio-8080-exec-1'
    },
    {
      timestamp: '2026-02-04T14:20:33.012Z',
      level: 'ERROR',
      message: 'Error occurred while processing repository: Invalid image format',
      logger: 'com.dockerplatform.backend.service.ImageService',
      thread: 'http-nio-8080-exec-7',
      stackTrace: 'java.lang.IllegalArgumentException: Invalid image format\n\tat com.dockerplatform...'
    },
    {
      timestamp: '2026-02-04T14:15:20.345Z',
      level: 'INFO',
      message: 'Repository nginx created successfully',
      logger: 'com.dockerplatform.backend.service.RepositoryService',
      thread: 'http-nio-8080-exec-2'
    },
    {
      timestamp: '2026-02-04T14:10:55.678Z',
      level: 'DEBUG',
      message: 'Validating user permissions for repository access',
      logger: 'com.dockerplatform.backend.security.PermissionValidator',
      thread: 'http-nio-8080-exec-4'
    },
    {
      timestamp: '2026-02-04T14:05:12.901Z',
      level: 'WARNING',
      message: 'Rate limit approaching for user admin: 95/100 requests',
      logger: 'com.dockerplatform.backend.filter.RateLimitFilter',
      thread: 'http-nio-8080-exec-6'
    },
    {
      timestamp: '2026-02-04T14:00:00.234Z',
      level: 'INFO',
      message: 'Application started successfully',
      logger: 'com.dockerplatform.backend.Application',
      thread: 'main'
    },
    {
      timestamp: '2026-02-04T13:55:45.567Z',
      level: 'ERROR',
      message: 'Error occurred during tag deletion: Tag not found',
      logger: 'com.dockerplatform.backend.service.TagService',
      thread: 'http-nio-8080-exec-8'
    },
    {
      timestamp: '2026-02-04T13:50:30.890Z',
      level: 'INFO',
      message: 'Elasticsearch health check passed',
      logger: 'com.dockerplatform.backend.config.ElasticsearchConfig',
      thread: 'scheduled-task-1'
    }
  ];

  constructor(private http: HttpClient) { }

  searchLogs(query: LogSearchQuery): Observable<LogSearchResponse> {
    // mock
    // TODO: napraviti pravi request
    return of(this.filterMockLogs(query));
  }

  private filterMockLogs(query: LogSearchQuery): LogSearchResponse {
    // mock
    // TODO: napraviti prave requestove
    let filtered = [...this.mockLogs];

    // filtriranje po datumima
    if (query.startDate) {
      const startDate = new Date(query.startDate);
      filtered = filtered.filter(log => new Date(log.timestamp) >= startDate);
    }
    if (query.endDate) {
      const endDate = new Date(query.endDate);
      filtered = filtered.filter(log => new Date(log.timestamp) <= endDate);
    }

    // filtriranje po vrsti logova
    if (query.levels && query.levels.length > 0) {
      filtered = filtered.filter(log => query.levels!.includes(log.level));
    }

    // filtriranje po tekstu
    if (query.query && query.query.trim()) {
      const searchTerm = query.query.toLowerCase();
      filtered = filtered.filter(log =>
        log.message.toLowerCase().includes(searchTerm) ||
        (log.logger && log.logger.toLowerCase().includes(searchTerm))
      );
    }

    // sortiranje
    filtered.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());

    // paginacija
    const from = query.from || 0;
    const size = query.size || 50;
    const paginated = filtered.slice(from, from + size);

    return {
      hits: paginated,
      total: filtered.length,
      took: 15
    };
  }

  exportLogs(query: LogSearchQuery) {
    // mock
    // TODO: dodati download kao csv ili json npr
    alert("export logova")
  }
}
