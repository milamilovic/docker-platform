import {Component, OnInit, signal} from '@angular/core';
import {UserDto} from '../register/user.model';
import {UserService} from '../register/user.service';

@Component({
  selector: 'app-admins',
  standalone: false,
  templateUrl: './admins.html',
  styleUrl: './admins.css',
})
export class Admins implements OnInit {
  admins = signal<any[]>([]);
  displayAddAdmin: boolean = false;
  constructor(private service: UserService) {}

  newAdmin: UserDto = {
    username: '',
    email: '',
    password: '',
  };

  ngOnInit(): void {
    this.loadAdmins();
  }

  loadAdmins() {
    this.service.getAdmins().subscribe(res => {
      this.admins.set(res);
    });
  }

  showAddAdminDialog() {
    this.displayAddAdmin = true;
  }

  saveAdmin() {
    this.service.registerAdmin(this.newAdmin).subscribe({
      next: () => {
        this.displayAddAdmin = false;
        this.loadAdmins();
        this.newAdmin = { username: '', email: ''};
      },
      error: (err) => console.error('Greška pri čuvanju admina', err)
    });
  }
}
