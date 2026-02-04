import { Injectable } from '@angular/core';
import { Observable, of, delay } from 'rxjs';
import { Repository, RepositoryDto, RepositoryUpdateDto } from '../models/repository.model';
import { AuthService } from '../../auth/auth.service';

@Injectable({
  providedIn: 'root'
})
export class RepositoryService {
  
  private mockRepositories: Repository[] = [
    {
      id: '1',
      name: 'nginx',
      ownerId: 'user1',
      ownerUsername: 'johndoe',
      description: 'Official build of Nginx',
      createdAt: Date.now() - 86400000 * 30,
      modifiedAt: Date.now() - 86400000 * 5,
      numberOfPulls: 150000,
      numberOfStars: 1200,
      isPublic: true,
      isOfficial: true
    },
    {
      id: '2',
      name: 'my-app',
      ownerId: 'user1',
      ownerUsername: 'johndoe',
      description: 'My custom application',
      createdAt: Date.now() - 86400000 * 15,
      modifiedAt: Date.now() - 86400000 * 2,
      numberOfPulls: 450,
      numberOfStars: 12,
      isPublic: true,
      isOfficial: false
    },
    {
      id: '3',
      name: 'private-api',
      ownerId: 'user1',
      ownerUsername: 'johndoe',
      description: 'Internal API service',
      createdAt: Date.now() - 86400000 * 7,
      numberOfPulls: 50,
      numberOfStars: 3,
      isPublic: false,
      isOfficial: false
    },
    {
      id: '4',
      name: 'redis',
      ownerId: 'admin',
      ownerUsername: 'admin',
      description: 'Official Redis image',
      createdAt: Date.now() - 86400000 * 60,
      modifiedAt: Date.now() - 86400000 * 1,
      numberOfPulls: 500000,
      numberOfStars: 3500,
      isPublic: true,
      isOfficial: true
    }
  ];

  constructor(private authService: AuthService) { }

  getMyRepositories(): Observable<Repository[]> {
    const userRole = this.authService.getRole();
    const currentUserId = this.authService.getUserId();
    let userRepos: Repository[];
    
    if (userRole === 'ADMIN') {
      // return all if admin
      userRepos = this.mockRepositories;
    } else {
      // just private if regular user
      userRepos = this.mockRepositories.filter(r => r.ownerId === currentUserId);
    }
    
    return of(userRepos);
  }

  getAllRepositories(): Observable<Repository[]> {
    return of(this.mockRepositories);
  }

  getOfficialRepositories(): Observable<Repository[]> {
    const officialRepos = this.mockRepositories.filter(r => r.isOfficial);
    return of(officialRepos);
  }

  getRepositoryById(id: string): Observable<Repository | undefined> {
    const repo = this.mockRepositories.find(r => r.id === id);
    return of(repo);
  }

  createRepository(dto: RepositoryDto): Observable<Repository> {
    const newRepo: Repository = {
      id: Math.random().toString(36).substr(2, 9),
      name: dto.name,
      ownerId: 'user1',
      ownerUsername: 'johndoe',
      description: dto.description,
      createdAt: Date.now(),
      numberOfPulls: 0,
      numberOfStars: 0,
      isPublic: dto.isPublic,
      isOfficial: dto.isOfficial || false
    };
    this.mockRepositories.push(newRepo);
    return of(newRepo);
  }

  updateRepository(id: string, dto: RepositoryUpdateDto): Observable<Repository> {
    const index = this.mockRepositories.findIndex(r => r.id === id);
    if (index !== -1) {
      this.mockRepositories[index] = {
        ...this.mockRepositories[index],
        ...dto,
        modifiedAt: Date.now()
      };
      return of(this.mockRepositories[index]);
    }
    throw new Error('Repository not found');
  }

  deleteRepository(id: string): Observable<void> {
    const index = this.mockRepositories.findIndex(r => r.id === id);
    if (index !== -1) {
      this.mockRepositories.splice(index, 1);
    }
    return of(void 0);
  }
}