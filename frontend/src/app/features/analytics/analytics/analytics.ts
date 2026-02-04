import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { LogEntry, LogSearchResponse, LogSearchQuery} from '../models/log.model';
import {LogLevel} from '../enums/log-level.enum';
import { AnalyticsService} from '../services/analytics.service';

@Component({
  selector: 'app-analytics',
  standalone: false,
  templateUrl: './analytics.html',
  styleUrl: './analytics.css',
  providers: [MessageService]
})
export class Analytics implements OnInit {
  searchForm: FormGroup;
  searchResults: LogEntry[] = [];
  totalResults: number = 0;
  searchTook: number = 0;
  loading: boolean = false;
  searchPerformed: boolean = false;
  expandedLog: LogEntry | null = null;
  currentPage: number = 0;
  pageSize: number = 20;
  totalPages: number = 0;

  // filters
  logLevels: string[] = Object.values(LogLevel);
  selectedLevels: string[] = [];

  pageSizeOptions = [
    { label: '10', value: 10 },
    { label: '20', value: 20 },
    { label: '50', value: 50 },
    { label: '100', value: 100 }
  ];

  constructor(
    private fb: FormBuilder,
    private messageService: MessageService,
    private analyticsService: AnalyticsService
  ) {
    this.searchForm = this.fb.group({
      query: [''],
      startDate: [''],
      endDate: ['']
    });
  }

  ngOnInit(): void {
    this.onSearch();
  }

  onSearch(): void {
    this.loading = true;
    this.currentPage = 0;
    this.performSearch();
  }

  performSearch(): void {
    const formValue = this.searchForm.value;

    const query: LogSearchQuery = {
      query: formValue.query || '',
      startDate: formValue.startDate ? new Date(formValue.startDate).toISOString() : undefined,
      endDate: formValue.endDate ? new Date(formValue.endDate).toISOString() : undefined,
      levels: this.selectedLevels.length > 0 ? this.selectedLevels : undefined,
      size: this.pageSize,
      from: this.currentPage * this.pageSize
    };

    this.analyticsService.searchLogs(query).subscribe({
      next: (response) => {
        this.searchResults = response.hits;
        this.totalResults = response.total;
        this.searchTook = response.took;
        this.totalPages = Math.ceil(response.total / this.pageSize);
        this.searchPerformed = true;
        this.loading = false;
        this.expandedLog = null;
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Search Failed',
          detail: 'Failed to search logs. Please try again.'
        });
        this.loading = false;
      }
    });
  }

  clearSearch(): void {
    this.searchForm.reset();
    this.selectedLevels = [];
    this.currentPage = 0;
    this.onSearch();
  }

  applyQuickFilter(filter: string): void {
    const now = new Date();
    let startDate: Date;

    switch (filter) {
      case '1h':
        startDate = new Date(now.getTime() - 60 * 60 * 1000);
        this.searchForm.patchValue({
          startDate: this.formatDateForInput(startDate),
          endDate: this.formatDateForInput(now)
        });
        break;
      case '24h':
        startDate = new Date(now.getTime() - 24 * 60 * 60 * 1000);
        this.searchForm.patchValue({
          startDate: this.formatDateForInput(startDate),
          endDate: this.formatDateForInput(now)
        });
        break;
      case '7d':
        startDate = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
        this.searchForm.patchValue({
          startDate: this.formatDateForInput(startDate),
          endDate: this.formatDateForInput(now)
        });
        break;
      case 'errors':
        this.selectedLevels = ['ERROR'];
        break;
    }

    this.onSearch();
  }

  toggleExpand(log: LogEntry): void {
    this.expandedLog = this.expandedLog === log ? null : log;
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.performSearch();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.performSearch();
    }
  }

  onPageSizeChange(): void {
    this.currentPage = 0;
    this.performSearch();
  }

  exportLogs(): void {
    const formValue = this.searchForm.value;

    const query: LogSearchQuery = {
      query: formValue.query || '',
      startDate: formValue.startDate ? new Date(formValue.startDate).toISOString() : undefined,
      endDate: formValue.endDate ? new Date(formValue.endDate).toISOString() : undefined,
      levels: this.selectedLevels.length > 0 ? this.selectedLevels : undefined,
      size: 10000 // Export more results
    };

    this.analyticsService.exportLogs(query);
  }

  formatTimestamp(timestamp: string): string {
    const date = new Date(timestamp);
    return date.toLocaleString('sr-RS', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  }

  private formatDateForInput(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }
}
