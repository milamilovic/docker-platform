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
// import { AvatarGroupModule } from 'primeng/avatargroup';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { Hello } from './features/hello/hello';
import { Navbar } from './core/navbar/navbar';
import { Home } from './features/home/home';
import { Sidebar } from './core/sidebar/sidebar';

@NgModule({
  declarations: [
    App,
    Hello,
    Navbar,
    Home,
    Sidebar,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ButtonModule,
    InputTextModule, 
    IconFieldModule, 
    InputIconModule,
    AvatarModule,
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
