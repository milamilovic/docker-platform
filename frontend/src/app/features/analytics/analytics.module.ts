import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Analytics } from './analytics/analytics';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

// PrimeNG modules
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { AvatarModule } from 'primeng/avatar';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { PasswordModule } from 'primeng/password';
import { CheckboxModule } from 'primeng/checkbox';
import { SelectModule } from 'primeng/select';

@NgModule({
  declarations: [
    Analytics
  ],
  imports: [
    CommonModule,
    ButtonModule,
    InputTextModule,
    IconFieldModule,
    InputIconModule,
    AvatarModule,
    DialogModule,
    FormsModule,
    ReactiveFormsModule,
    ToastModule,
    PasswordModule,
    CheckboxModule,
    SelectModule
  ],
  exports: [
    Analytics
  ]
})
export class AnalyticsModule { }
