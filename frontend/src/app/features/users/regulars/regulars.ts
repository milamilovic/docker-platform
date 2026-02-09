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
  badgeOptions = [
    { label: 'Docker Official Image', value: 'DOCKER_OFFICIAL_IMAGE' },
    { label: 'Verified Publisher', value: 'VERIFIED_PUBLISHER' },
    { label: 'Sponsored OSS', value: 'SPONSORED_OSS' },
    { label: 'None', value: null }
  ];

  onBadgeChange(admin: any) {

    this.service.addBadge(admin.username, admin.badge).subscribe({
      next: (response) => {
        this.messageService.add({ severity: 'success', summary: 'Updated', detail: 'Badge successfully changed' });
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to update badge' });
      }
    });
  }

  getBadgeClass(badge: string): string {
    switch (badge) {
      case 'DOCKER_OFFICIAL_IMAGE': return 'official-badge';
      case 'VERIFIED_PUBLISHER': return 'verified-badge';
      case 'SPONSORED_OSS': return 'sponsored-badge';
      default: return '';
    }
  }

}
