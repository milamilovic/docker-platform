import {Component, OnInit, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import { env } from './shared/env';
import {AuthService} from './features/auth/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  standalone: false,
  styleUrl: './app.css'
})
export class App implements OnInit {
  protected readonly title = signal('frontend');
  displayLockdown = signal(false);

  constructor(private http: HttpClient, private auth: AuthService) {}

  ngOnInit(): void {
    this.auth.status().subscribe({
      next: (isLocked: any) => {
        this.displayLockdown.set(isLocked);
      },
      error: (err) => {
        if (err.status === 503) {
          this.displayLockdown.set(false);
        }
      }
    });
  }

  setupData = {
    username: '',
    oldPassword: '',
    newPassword: ''
  };

  submitSetup() {
    this.http.post(`${env.apiUrl}/auth/initialize`, this.setupData).subscribe({
      next: () => {
        this.displayLockdown.set(false)
        console.log('Sistem uspešno inicijalizovan');
      },
      error: (err) => {
        console.error('Greška pri inicijalizaciji', err);
      }
    });
  }
}
