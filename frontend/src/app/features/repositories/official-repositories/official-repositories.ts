import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { Repository, RepositoryDto } from '../models/repository.model';
import { RepositoryService } from '../services/repository.service';

@Component({
  selector: 'app-official-repositories',
  standalone: false,
  templateUrl: './official-repositories.html',
  styleUrl: './official-repositories.css',
  providers: [MessageService]
})
export class OfficialRepositories implements OnInit {
  repositories: Repository[] = [];
  filteredRepositories: Repository[] = [];
  displayCreateDialog: boolean = false;
  loading: boolean = false;
  createForm: FormGroup;
  searchTerm: string = '';

  constructor(
    private fb: FormBuilder,
    private messageService: MessageService,
    private repositoryService: RepositoryService,
    private router: Router
  ) {
    this.createForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3), Validators.pattern(/^[a-z0-9-]+$/)]],
      description: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadRepositories();
  }

  loadRepositories(): void {
    this.repositoryService.getOfficialRepositories().subscribe({
      next: (repos) => {
        this.repositories = repos;
        this.filteredRepositories = repos;
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load official repositories'
        });
      }
    });
  }

  filterRepositories(): void {
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      this.filteredRepositories = this.repositories.filter(repo => 
        repo.name.toLowerCase().includes(term) ||
        repo.description.toLowerCase().includes(term)
      );
    } else {
      this.filteredRepositories = this.repositories;
    }
  }

  showCreateDialog(): void {
    this.displayCreateDialog = true;
  }

  hideCreateDialog(): void {
    this.displayCreateDialog = false;
    this.createForm.reset();
  }

  onCreate(): void {
    if (this.createForm.valid) {
      this.loading = true;
      const payload: RepositoryDto = {
        ...this.createForm.value,
        isPublic: true,
        isOfficial: true
      };
      
      this.repositoryService.createRepository(payload).subscribe({
        next: (response) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: `Official repository "${payload.name}" created successfully!`
          });
          this.hideCreateDialog();
          this.loading = false;
          this.loadRepositories();
        },
        error: (err) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to create official repository'
          });
          this.loading = false;
        }
      });
    }
  }

  viewDetails(id: string): void {
    this.router.navigate(['/repositories', id]);
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