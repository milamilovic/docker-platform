import { Injectable } from '@angular/core';
import { env } from '../../../shared/env';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class StarService {
  private readonly API_URL = `${env.apiUrl}/stars`;

  constructor(private http: HttpClient) { }

  // TODO: 
  loadStar(userId: string, repoId: string) {
    //
  }

  toggleStar(userId: string, repoId: string) {
    //
  }

  loadMyStarred(userId: string) {
    //
  }
}
