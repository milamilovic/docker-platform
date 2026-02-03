import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { AuthService} from './auth.service';

@Component({
  selector: 'app-auth',
  standalone: false,
  templateUrl: './auth.html',
  styleUrl: './auth.css',
  providers: [MessageService]
})
export class Auth {
loginForm: FormGroup;
  display: boolean = false;
  loading: boolean = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private messageService: MessageService
  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });
  }

  showDialog() {
    this.display = true;
  }

  onSubmit() {
    if (this.loginForm.valid) {
    this.loading = true;
    this.authService.logIn(this.loginForm.value).subscribe({
      next: (token) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Welcome back',
          detail: `Login successful!`
        });
        this.loading = false;
        this.display = false;
        localStorage.setItem('token', token.token);
        this.authService.loggedIn$.next(true);
        this.authService.userRole$.next(this.authService.getRole());
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Login Failed',
          detail: 'Invalid username or password.'
        });
        this.loading = false;
      }
    })
    }
  }
}
