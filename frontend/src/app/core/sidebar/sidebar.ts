import { Component } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { AuthService } from '../../features/auth/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: false,
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar {
    checked: any = null;

    filtersForm = new FormGroup({
        isOfficial: new FormControl(false),
        isVerified: new FormControl(false),
        isSponsored: new FormControl(false),
    })

  constructor(private authService: AuthService) {}

  isAdmin(): boolean {
    return this.authService.getRole() === 'ADMIN';
  }

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }
}
