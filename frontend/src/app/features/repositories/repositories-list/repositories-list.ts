import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { Repository, RepositoryDto } from '../models/repository.model';
import { RepositoryService } from '../services/repository.service';

@Component({
  selector: 'app-repositories-list',
  standalone: false,
  templateUrl: './repositories-list.html',
  styleUrl: './repositories-list.css',
  providers: [MessageService]
})
export class RepositoriesList implements OnInit {
  repositories: Repository[] = [];
  filteredRepositories: Repository[] = [];
  displayCreateDialog: boolean = false;
  loading: boolean = false;
  createForm: FormGroup;
  searchTerm: string = '';
  selectedVisibility: string = 'all';

  visibilityOptions = [
    { label: 'All Repositories', value: 'all' },
    { label: 'Public', value: 'public' },
    { label: 'Private', value: 'private' }
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
  }

  loadRepositories(): void {
    this.repositoryService.getMyRepositories().subscribe({
      next: (repos) => {
        this.repositories = repos;
        this.filteredRepositories = repos;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load repositories'
        });
      }
    });
  }

  filterRepositories(): void {
    let filtered = this.repositories;

    // Filter by search term
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(repo => 
        repo.name.toLowerCase().includes(term) ||
        repo.description.toLowerCase().includes(term)
      );
    }

    // Filter by visibility
    if (this.selectedVisibility !== 'all') {
      filtered = filtered.filter(repo => 
        this.selectedVisibility === 'public' ? repo.isPublic : !repo.isPublic
      );
    }

    this.filteredRepositories = filtered;
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