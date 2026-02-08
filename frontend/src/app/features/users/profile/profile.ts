import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {UserService} from '../user.service';
import {MessageService} from 'primeng/api';
import {UserInfo} from '../user.model';
import {AuthService} from '../../auth/auth.service';

@Component({
  selector: 'app-profile',
  standalone: false,
  templateUrl: './profile.html',
  styleUrl: './profile.css',
  providers: [MessageService]
})
export class Profile {
  private _visible: boolean = false;

  @Input()
  get visible(): boolean { return this._visible; }
  set visible(val: boolean) {
    this._visible = val;
    if (val) {
      this.loadFreshUserData();
    }
  }
  @Output() visibleChange = new EventEmitter<boolean>();

  userInfo: UserInfo = {
    username: "",
    email: "",
  };

  passwordData = {
    username: '',
    password: '',
    newPassword: ''
  };

  constructor(private userService: UserService,
              private messageService: MessageService,
              private auth:AuthService) {}

  loadFreshUserData(): void {
    this.userService.getUserInfo(this.auth.getUserId().toString()).subscribe({
      next: (user) => {
        this.userInfo = user;
      },
      error: (err) => console.error("Could not fetch user info", err)
    })
  }

  close() {
    this.visible = false;
    this.visibleChange.emit(false);
    this.userInfo = { username: "", email: "" };
    this.passwordData = { username: '', password: '', newPassword: '' };
  }

  updateProfile() {
    this.passwordData.username = this.userInfo.username;
    this.userService.changePassword(this.passwordData).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Updated', detail: 'Password changed!' });
        this.close();
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Invalid current password.' });
      }
    });
  }
}
