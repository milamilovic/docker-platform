import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { Repository, RepositoryUpdateDto } from '../models/repository.model';
import { Tag } from '../models/tag.model';
import { SpringPage } from '../models/spring-page.model';
import { RepositoryService } from '../services/repository.service';
import { TagService } from '../services/tag.service';
import { debounceTime, Subject } from 'rxjs';

@Component({
  selector: 'app-repository-details',
  standalone: false,
  templateUrl: './repository-details.html',
  styleUrl: './repository-details.css',
  providers: [MessageService]
})
export class RepositoryDetails implements OnInit {
  repository?: Repository;
  tags: Tag[] = [];
  activeTab: string = 'tags';
  settingsForm: FormGroup;
  loadingUpdate: boolean = false;
  loadingDelete: boolean = false;
  displayDeleteTagDialog: boolean = false;
  displayDeleteRepoDialog: boolean = false;
  tagToDelete?: Tag;
  confirmDeleteName: string = '';
  
  // Tags pagination
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
  
  // Tags filtering and sorting
  searchTag: string = '';
  selectedSort: string = 'pushedAt,desc';
  
  // Debounce search
  private searchSubject = new Subject<string>();

  sortOptions = [
    { label: 'Newest First', value: 'pushedAt,desc' },
    { label: 'Oldest First', value: 'pushedAt,asc' },
    { label: 'Name (A-Z)', value: 'name,asc' },
    { label: 'Name (Z-A)', value: 'name,desc' },
    { label: 'Size (Largest)', value: 'size,desc' },
    { label: 'Size (Smallest)', value: 'size,asc' }
  ];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private messageService: MessageService,
    private repositoryService: RepositoryService,
    private tagService: TagService,
    private cdr: ChangeDetectorRef
  ) {
    this.settingsForm = this.fb.group({
      description: ['', Validators.required],
      isPublic: [true, Validators.required]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadRepository(id);
      this.loadTags(id);
    }
    
    // debounced search
    this.searchSubject.pipe(debounceTime(300)).subscribe(() => {
      this.currentPage = 0;
      if (this.repository) {
        this.loadTags(this.repository.id);
      }
    });
  }

  loadRepository(id: string): void {
    this.repositoryService.getRepositoryById(id).subscribe({
      next: (repo) => {
        if (repo) {
          this.repository = repo;
          this.settingsForm.patchValue({
            description: repo.description,
            isPublic: repo.isPublic
          });
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Repository not found'
          });
          this.router.navigate(['/repositories']);
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load repository'
        });
      }
    });
  }

  loadTags(repositoryId: string): void {
    this.tagService.getTagsByRepository(
      repositoryId,
      this.currentPage,
      this.pageSize,
      this.selectedSort,
      this.searchTag || undefined
    ).subscribe({
      next: (response: SpringPage<Tag>) => {
        this.tags = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load tags'
        });
      }
    });
  }

  onSearchTag(): void {
    this.searchSubject.next(this.searchTag);
  }

  onSortChange(event: any): void {
    this.selectedSort = event.value;
    this.currentPage = 0;
    if (this.repository) {
      this.loadTags(this.repository.id);
    }
  }

  onPageChange(event: any): void {
    this.currentPage = event.page;
    this.pageSize = event.rows;
    if (this.repository) {
      this.loadTags(this.repository.id);
    }
  }

  get totalResults(): number {
    return this.totalElements;
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      if (this.repository) {
        this.loadTags(this.repository.id);
      }
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      if (this.repository) {
        this.loadTags(this.repository.id);
      }
    }
  }

  onPageSizeChange(): void {
    this.currentPage = 0;
    if (this.repository) {
      this.loadTags(this.repository.id);
    }
  }

  updateSettings(): void {
    if (this.settingsForm.valid && this.repository) {
      this.loadingUpdate = true;
      const payload: RepositoryUpdateDto = this.settingsForm.value;

      this.repositoryService.updateRepository(this.repository.id, payload).subscribe({
        next: (response) => {
          this.repository = response;
          this.settingsForm.markAsPristine();
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Repository settings updated successfully'
          });
          this.loadingUpdate = false;
        },
        error: (err) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to update repository'
          });
          this.loadingUpdate = false;
        }
      });
    }
  }

  resetSettings(): void {
    if (this.repository) {
      this.settingsForm.patchValue({
        description: this.repository.description,
        isPublic: this.repository.isPublic
      });
      this.settingsForm.markAsPristine();
    }
  }

  confirmDeleteTag(tag: Tag): void {
    this.tagToDelete = tag;
    this.displayDeleteTagDialog = true;
  }

  deleteTag(): void {
    if (this.tagToDelete) {
      this.loadingDelete = true;
      this.tagService.deleteTag(this.tagToDelete.id).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: `Tag "${this.tagToDelete?.name}" deleted successfully`
          });
          this.displayDeleteTagDialog = false;
          this.loadingDelete = false;
          if (this.repository) {
            this.loadTags(this.repository.id);
          }
        },
        error: (err) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to delete tag'
          });
          this.loadingDelete = false;
        }
      });
    }
  }

  confirmDeleteRepository(): void {
    this.displayDeleteRepoDialog = true;
  }

  deleteRepository(): void {
    if (this.repository && this.confirmDeleteName === this.repository.name) {
      this.loadingDelete = true;
      this.repositoryService.deleteRepository(this.repository.id).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Repository deleted successfully'
          });
          this.loadingDelete = false;
          this.router.navigate(['/repositories']);
        },
        error: (err) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to delete repository'
          });
          this.loadingDelete = false;
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/repositories']);
  }

  getRepositoryDisplayName(): string {
    if (!this.repository) return '';
    return this.repository.isOfficial 
      ? this.repository.name 
      : `${this.repository.ownerUsername}/${this.repository.name}`;
  }

  formatNumber(num: number): string {
    if (num >= 1000000) {
      return (num / 1000000).toFixed(1) + 'M';
    } else if (num >= 1000) {
      return (num / 1000).toFixed(1) + 'K';
    }
    return num.toString();
  }

  formatSize(bytes: number): string {
    if (bytes >= 1073741824) {
      return (bytes / 1073741824).toFixed(2) + ' GB';
    } else if (bytes >= 1048576) {
      return (bytes / 1048576).toFixed(2) + ' MB';
    } else if (bytes >= 1024) {
      return (bytes / 1024).toFixed(2) + ' KB';
    }
    return bytes + ' B';
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