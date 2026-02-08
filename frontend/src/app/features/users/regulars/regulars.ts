import {Component, OnInit, signal} from '@angular/core';
import {UserService} from '../user.service';
import {MessageService} from 'primeng/api';
import {UserDto} from '../user.model';

@Component({
  selector: 'app-regulars',
  standalone: false,
  templateUrl: './regulars.html',
  styleUrl: './regulars.css',
  providers: [MessageService]
})
export class Regulars implements OnInit {
  regulars = signal<any[]>([]);
  constructor(private service: UserService, private messageService: MessageService) {}


  ngOnInit(): void {
    this.loadAdmins();
  }

  loadAdmins() {
    this.service.getRegulars().subscribe(res => {
      this.regulars.set(res);
    });
  }


}
