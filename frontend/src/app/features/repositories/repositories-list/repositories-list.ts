import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { Repository, RepositoryDto } from '../models/repository.model';
import { SpringPage } from '../models/spring-page.model';
import { RepositoryService } from '../services/repository.service';
import { debounceTime, Subject } from 'rxjs';

@Component({
  selector: 'app-repositories-list',
  standalone: false,
  templateUrl: './repositories-list.html',
  styleUrl: './repositories-list.css',
  providers: [MessageService]
})
export class RepositoriesList implements OnInit {
  repositories: Repository[] = [];
  displayCreateDialog: boolean = false;
  loading: boolean = false;
  createForm: FormGroup;
  
  // Pagination
  currentPage: number = 0;
  pageSize: number = 10;
  totalElements: number = 0;
  totalPages: number = 0;
  pageSizeOptions = [
    { label: '5', value: 5 },
    { label: '10', value: 10 },
    { label: '20', value: 20 },
    { label: '50', value: 50 }
  ];
  
  // Filtering and sorting
  searchTerm: string = '';
  selectedVisibility: string = 'all';
  selectedSort: string = 'modifiedAt,desc';
  
  // Debounce search
  private searchSubject = new Subject<string>();

  visibilityOptions = [
    { label: 'All Repositories', value: 'all' },
    { label: 'Public', value: 'public' },
    { label: 'Private', value: 'private' }
  ];

  sortOptions = [
    { label: 'Recently Updated', value: 'modifiedAt,desc' },
    { label: 'Recently Created', value: 'createdAt,desc' },
    { label: 'Name (A-Z)', value: 'name,asc' },
    { label: 'Name (Z-A)', value: 'name,desc' },
    { label: 'Most Pulls', value: 'numberOfPulls,desc' },
    { label: 'Most Stars', value: 'numberOfStars,desc' }
  ];

  constructor(
    private fb: FormBuilder,
    private messageService: MessageService,
    private repositoryService: RepositoryService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.createForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3), Validators.pattern(/^[a-z0-9-]+$/)]],
      description: ['', Validators.required],
      isPublic: [true, Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadRepositories();
    
    // debounced search
    this.searchSubject.pipe(debounceTime(300)).subscribe(() => {
      this.currentPage = 0;
      this.loadRepositories();
    });
  }

  loadRepositories(): void {
    this.loading = true;
    this.repositoryService.getMyRepositories(
      this.currentPage,
      this.pageSize,
      this.selectedSort,
      this.searchTerm || undefined,
      this.selectedVisibility
    ).subscribe({
      next: (response: SpringPage<Repository>) => {
        this.repositories = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load repositories'
        });
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    this.searchSubject.next(this.searchTerm);
  }

  onVisibilityChange(): void {
    this.currentPage = 0;
    this.loadRepositories();
  }

  onSortChange(event: any): void {
    this.selectedSort = event.value;
    this.currentPage = 0;
    this.loadRepositories();
  }

  get totalResults(): number {
    return this.totalElements;
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadRepositories();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadRepositories();
    }
  }

  onPageSizeChange(): void {
    this.currentPage = 0;
    this.loadRepositories();
  }

  showCreateDialog(): void {
    this.displayCreateDialog = true;
  }

  hideCreateDialog(): void {
    this.displayCreateDialog = false;
    this.createForm.reset({ isPublic: true });
  }

  onCreate(): void {
    if (this.createForm.valid) {
      this.loading = true;
      const payload: RepositoryDto = {
        ...this.createForm.value,
        isOfficial: false
      };

      this.repositoryService.createRepository(payload).subscribe({
        next: (response) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: `Repository "${payload.name}" created successfully!`
          });
          this.hideCreateDialog();
          this.loading = false;
          this.currentPage = 0;
          this.loadRepositories();
        },
        error: (err) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to create repository'
          });
          this.loading = false;
        }
      });
    }
  }

  viewDetails(id: string): void {
    this.router.navigate(['/repositories', id]);
  }

  getRepositoryDisplayName(repo: Repository): string {
    return repo.isOfficial ? repo.name : `${repo.ownerUsername}/${repo.name}`;
  }

  formatNumber(num: number): string {
    if (num >= 1000000) {
      return (num / 1000000).toFixed(1) + 'M';
    } else if (num >= 1000) {
      return (num / 1000).toFixed(1) + 'K';
    }
    return num.toString();
  }

  getRelativeTime(timestamp: number): string {
    const now = Date.now();
    const diff = now - timestamp;
    const days = Math.floor(diff / 86400000);
    
    if (days === 0) return 'today';
    if (days === 1) return 'yesterday';
    if (days < 7) return `${days} days ago`;
    if (days < 30) return `${Math.floor(days / 7)} weeks ago`;
    return `${Math.floor(days / 30)} months ago`;
  }
}