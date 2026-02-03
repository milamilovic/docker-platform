import {Inject, Injectable, PLATFORM_ID} from '@angular/core';
import { env } from '../../shared/env';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {BehaviorSubject, Observable} from 'rxjs';
import { jwtDecode } from 'jwt-decode';
import {isPlatformBrowser} from '@angular/common';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private readonly API_URL = `${env.apiUrl}/auth`;

  loggedIn$ = new BehaviorSubject<boolean>(false);
  loggedInState = this.loggedIn$.asObservable();

  userRole$ = new BehaviorSubject<string>('');
  userRoleState = this.userRole$.asObservable();

  constructor(@Inject(PLATFORM_ID) private platformId: Object,
              private http: HttpClient,) {
    this.loggedIn$.next(this.isLoggedIn());
    this.userRole$.next(this.getRole());
  }

  // private headers = new HttpHeaders({
  //   'Content-Type': 'application/json',
  //   skip: 'true',
  // });

  logIn(credentials: any): Observable<any> {
    return this.http.post<any>(this.API_URL, credentials);
  }
  logOut(): void {
    localStorage.clear();
    this.loggedIn$.next(false);
  }

  isLoggedIn(): boolean {
    this.getUserId();
    if (isPlatformBrowser(this.platformId)) {
      return !!localStorage.getItem('token');
    }
    return false;
  }

  private getDecodedToken(token:any):any{
    if (!token) return null;
    const decodedToken = jwtDecode(token);
    const jsonString = JSON.stringify(decodedToken);
    const parsed = JSON.parse(jsonString);
    return parsed;
  }

  getUserId(): string {
    const decodedToken = this.getDecodedToken(localStorage.getItem('token'));
    if (decodedToken) {
      return decodedToken.id;
    }
    return '';
  }

  getRole(): string {
    const decodedToken = this.getDecodedToken(localStorage.getItem('token'));
    if (decodedToken) {
      return decodedToken.role;
    }
    return '';
  }

  private isTokenExpired(token:any): boolean {
    const decodedToken = this.getDecodedToken(token);
    if (decodedToken) {
      const currentTime = Math.floor(Date.now() / 1000);
      return decodedToken.exp < currentTime;
    }
    return false;
  }

  isAccessTokenExpired(): boolean {
    return this.isTokenExpired(localStorage.getItem('token'));
  }

}
