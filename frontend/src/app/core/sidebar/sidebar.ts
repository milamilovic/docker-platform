import { Component } from '@angular/core';
import { AuthService } from '../../features/auth/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: false,
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar {

  constructor(private authService: AuthService) {}

  isAdmin(): boolean {
    return this.authService.getRole() === 'ADMIN';
  }

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }
}