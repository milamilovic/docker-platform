import {Component, OnInit, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import { env } from './shared/env';
import {AuthService,ChPassword} from './features/auth/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  standalone: false,
  styleUrl: './app.css'
})
export class App implements OnInit {
  protected readonly title = signal('frontend');
  displayLockdown = signal(false);
  setupData:ChPassword = {
    newPassword: '',
    password: '',
    username: ''
  };

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


  submitSetup() {
    this.auth.initSystem(this.setupData).subscribe({
      next: (isInit) => {
        if (isInit) {
          this.displayLockdown.set(false)
          console.log('Sistem uspešno inicijalizovan');
        }
        console.error('Greška pri inicijalizaciji');
      },
      error: (err) => {
        console.error('Greška pri inicijalizaciji', err);
      }
    });
  }
}
