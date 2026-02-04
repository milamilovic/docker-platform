import { Injectable } from '@angular/core';
import { Observable, of, delay } from 'rxjs';
import { Tag } from '../models/tag.model';

@Injectable({
  providedIn: 'root'
})
export class TagService {
  
  private mockTags: Tag[] = [
    {
      id: '1',
      name: 'latest',
      digest: 'sha256:abc123def456',
      size: 142567890,
      createdAt: Date.now() - 86400000 * 5,
      pushedAt: Date.now() - 86400000 * 2,
      repositoryId: '1'
    },
    {
      id: '2',
      name: 'v1.0.0',
      digest: 'sha256:def789ghi012',
      size: 142567890,
      createdAt: Date.now() - 86400000 * 30,
      pushedAt: Date.now() - 86400000 * 30,
      repositoryId: '1'
    },
    {
      id: '3',
      name: 'alpine',
      digest: 'sha256:ghi345jkl678',
      size: 45678901,
      createdAt: Date.now() - 86400000 * 15,
      pushedAt: Date.now() - 86400000 * 15,
      repositoryId: '1'
    },
    {
      id: '4',
      name: 'latest',
      digest: 'sha256:mno901pqr234',
      size: 256789012,
      createdAt: Date.now() - 86400000 * 2,
      pushedAt: Date.now() - 86400000 * 1,
      repositoryId: '2'
    },
    {
      id: '5',
      name: 'dev',
      digest: 'sha256:stu567vwx890',
      size: 267890123,
      createdAt: Date.now() - 86400000 * 5,
      pushedAt: Date.now() - 86400000 * 3,
      repositoryId: '2'
    }
  ];

  constructor() { }

  getTagsByRepository(repositoryId: string): Observable<Tag[]> {
    const tags = this.mockTags.filter(t => t.repositoryId === repositoryId);
    return of(tags).pipe(delay(300));
  }

  deleteTag(id: string): Observable<void> {
    const index = this.mockTags.findIndex(t => t.id === id);
    if (index !== -1) {
      this.mockTags.splice(index, 1);
    }
    return of(void 0).pipe(delay(500));
  }
}