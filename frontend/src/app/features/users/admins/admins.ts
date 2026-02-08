import {Component, OnInit, signal} from '@angular/core';
import {UserDto} from '../user.model';
import {UserService} from '../user.service';
import {MessageService} from 'primeng/api';

@Component({
  selector: 'app-admins',
  standalone: false,
  templateUrl: './admins.html',
  styleUrl: './admins.css',
  providers: [MessageService]
})
export class Admins implements OnInit {
  admins = signal<any[]>([]);
  displayAddAdmin: boolean = false;
  constructor(private service: UserService, private messageService: MessageService) {}

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
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Admin successfully registered!'
        });
        this.newAdmin = { username: '', email: ''};
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to save admin. Username might already exist.'
        });
      }
    });
  }
}
