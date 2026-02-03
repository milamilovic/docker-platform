import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';

// PrimeNG modules
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { AvatarModule } from 'primeng/avatar';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { PasswordModule } from 'primeng/password';

// import { AvatarGroupModule } from 'primeng/avatargroup';

import { ReactiveFormsModule } from '@angular/forms';
import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { Hello } from './features/hello/hello';
import { Navbar } from './core/navbar/navbar';
import { Home } from './features/home/home';
import { Sidebar } from './core/sidebar/sidebar';
import { Register } from './features/register/register';

@NgModule({
  declarations: [
    App,
    Hello,
    Navbar,
    Home,
    Sidebar,
    Register,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ButtonModule,
    InputTextModule, 
    IconFieldModule, 
    InputIconModule,
    AvatarModule,
    DialogModule,
    ReactiveFormsModule,
    ToastModule,
    PasswordModule
    // AvatarGroupModule
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),
    // provideAnimationsAsync(), // Required for animations
    providePrimeNG({
        theme: {
            preset: Aura, 
            options: {
                darkModeSelector: 'system' 
            }
        }
    })
  ],
  bootstrap: [App]
})
export class AppModule { }
