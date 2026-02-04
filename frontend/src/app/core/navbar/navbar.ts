import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../features/auth/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: false,
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar implements OnInit {
    constructor(protected authService: AuthService) {}
    isLoggedIn = false;
    ngOnInit(): void {
          this.authService.loggedIn$.subscribe(loggedIn => {
            this.isLoggedIn = loggedIn;
          })
    }

}
