import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {UserDto, UserInfo} from './user.model';
import { env } from '../../shared/env'
import {AuthService, ChPassword} from '../auth/auth.service';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly API_URL = `${env.apiUrl}/user`;

  constructor(private http: HttpClient,private auth:AuthService) { }

  register(userDto: UserDto): Observable<UserDto> {
    return this.http.post<UserDto>(`${this.API_URL}/register`, userDto);
  }

  getAdmins():Observable<any>{
    return this.http.get<UserDto[]>(`${this.API_URL}/admins`)
  }

  registerAdmin(dto: UserDto): Observable<UserDto> {
    return this.http.post<UserDto>(`${this.API_URL}/admins`, dto);
  }

  changePassword(dto: ChPassword): Observable<boolean>{
    return this.http.put<boolean>(`${this.API_URL}`, dto);
  }

  getUserInfo(id:string): Observable<UserInfo> {
    return this.http.get<UserInfo>(`${this.API_URL}/${id}`);
  }
}
