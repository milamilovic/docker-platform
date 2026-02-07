import {Component, ElementRef, HostListener, OnInit, ViewChild} from '@angular/core';
import {AuthService} from '../../features/auth/auth.service';
import { FormControl } from '@angular/forms';
import { filter } from 'rxjs';
import { Router } from '@angular/router';

@Component({
  selector: 'app-navbar',
  standalone: false,
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar implements OnInit {

    isLoggedIn = false;
    searchControl = new FormControl('');

    constructor(protected authService: AuthService, private router: Router) {}
    
    ngOnInit(): void {
        this.authService.loggedIn$.subscribe(loggedIn => {
        this.isLoggedIn = loggedIn;
        })

        this.searchControl.valueChanges
        .pipe(filter(v => v!.length > 0))
        .subscribe();
    }

    @ViewChild('searchInput') 
    searchInput!: ElementRef<HTMLInputElement>;

    @HostListener('window:keydown', ['$event'])
    handleKey(e: KeyboardEvent) {
        if (e.key === '/' && document.activeElement?.tagName !== 'INPUT') {
            e.preventDefault();
            this.searchInput.nativeElement.focus();
        }
    }

    onEnter() {
        if (this.searchControl.value == null || this.searchControl.value == '') return; 

        this.router.navigate(['/search'], {
            queryParams: { q: this.searchControl.value }
        });
    }
}
