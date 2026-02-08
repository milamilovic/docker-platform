import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { LogEntry, LogSearchResponse, LogSearchQuery} from '../models/log.model';
import {LogLevel} from '../enums/log-level.enum';
import { AnalyticsService} from '../services/analytics.service';

interface RawLogEntry {
  '@timestamp': string;
  level: string;
  message: string;
  logger_name?: string;
  thread_name?: string;
  stack_trace?: string;
  additionalData?: any;
}

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
    private analyticsService: AnalyticsService,
    private cdr: ChangeDetectorRef
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

    let search = true;
    if (formValue.startDate && formValue.endDate) {
      const start = new Date(formValue.startDate);
      const end = new Date(formValue.endDate);
      const now = new Date();
      // endDate ne sme biti posle sada
      if (end > now) {
        console.log("End date must be in the past.");
        this.messageService.add({
          severity: 'warn',
          summary: 'Search Failed',
          detail: 'End date must be in the past.'
        });
        search = false;
        this.loading=false;
        this.searchResults=[];
        this.searchTook=0;
        this.totalResults=0;
      }
      // startDate mora biti pre endDate
      if (start >= end) {
        console.log("Start date must be before end date.");
        this.messageService.add({
          severity: 'warn',
          summary: 'Search Failed',
          detail: 'Start date must be before end date.'
        });
        search = false;
        this.loading=false;
        this.searchResults=[];
        this.searchTook=0;
        this.totalResults=0;
      }
    }

    if (search) {
      this.analyticsService.searchLogs(query).subscribe({
        next: (response: LogSearchResponse) => {
          const rawHits: RawLogEntry[] = response.hits as unknown as RawLogEntry[];
          this.searchResults = rawHits.map(hit => ({
            timestamp: hit['@timestamp'],
            level: hit.level,
            message: hit.message,
            logger: hit.logger_name,
            thread: hit.thread_name,
            stackTrace: hit.stack_trace,
            additionalData: hit.additionalData
          }));
          this.totalResults = response.total;
          this.searchTook = response.took;
          this.totalPages = Math.ceil(response.total / this.pageSize);
          this.searchPerformed = true;
          this.loading = false;
          this.expandedLog = null;
          this.cdr.detectChanges();
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
      size: 10000
    };

    this.analyticsService.exportLogs(query).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `dockerplatform-logs-export-${Date.now()}.json`;
        link.click();
        URL.revokeObjectURL(url);
        this.cdr.detectChanges();

        this.messageService.add({
          severity: 'success',
          summary: 'Export Successful',
          detail: 'Logs exported successfully'
        });
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Export Failed',
          detail: 'Failed to export logs'
        });
      }
    });
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
