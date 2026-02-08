import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { UserDto } from '../user.model';
import { UserService } from '../user.service';

@Component({
  selector: 'app-register',
  standalone: false,
  templateUrl: './register.html',
  styleUrl: './register.css',
  providers: [MessageService]
})
export class Register {
  registerForm: FormGroup;
  display: boolean = false;
  loading: boolean = false;

  constructor(
    private fb: FormBuilder,
    private messageService: MessageService,
    private userService: UserService,
  ) {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  showDialog(): void {
    this.display = true;
  }

  hideDialog(): void {
    this.display = false;
    this.registerForm.reset();
  }

  onSubmit(): void {
    if (this.registerForm.valid) {
      this.loading = true;
      const payload: UserDto = this.registerForm.value as UserDto;
      this.userService.register(payload).subscribe({
        next: (response) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Registration Successful',
            detail: `Welcome, ${payload.username}!`
          });
          this.hideDialog();
          this.loading = false;
        }
      })
    } else {
      this.messageService.add({
        severity: 'error',
        summary: 'Invalid Form',
        detail: 'Please check your input and try again.'
      });
      this.loading = false;
    }
  }
}
