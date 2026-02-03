import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserDto } from './user.model';
import { env } from '../../shared/env'

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly API_URL = `${env.apiUrl}/user`;

  constructor(private http: HttpClient) { }

  register(userDto: UserDto): Observable<UserDto> {
    return this.http.post<UserDto>(`${this.API_URL}/register`, userDto);
  }
}
