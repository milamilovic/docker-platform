import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { Repository } from '../../models/repository';
import { Router } from '@angular/router';

@Component({
  selector: 'app-repo-card',
  standalone: false,
  templateUrl: './repo-card.html',
  styleUrl: './repo-card.css',
  changeDetection: ChangeDetectionStrategy.OnPush 
})
export class RepoCard {
    @Input() repo!: Repository; 

    constructor(private router: Router) {}

    formatCount(count: number): string {
      if (count >= 1_000_000) {
        return (count / 1_000_000).toFixed(1).replace(/\.0$/, '') + 'M+';
      } else if (count >= 1_000) {
        return (count / 1_000).toFixed(1).replace(/\.0$/, '') + 'K+';
      } else {
        return count.toString();
      }
    }

    formatLastUpdated(timestamp: number): string {
        const now = Date.now();
        let diff = now - timestamp;

        if (diff < 0) diff = 0; 

        const msPerMinute = 1000 * 60;
        const msPerHour = msPerMinute * 60;
        const msPerDay = msPerHour * 24;
        const msPerMonth = msPerDay * 30;
        const msPerYear = msPerDay * 365;

        if (diff < msPerHour) {
            const minutes = Math.round(diff / msPerMinute);
            return `about ${minutes} minute${minutes > 1 ? 's' : ''}`;
        } else if (diff < msPerDay) {
            const hours = Math.round(diff / msPerHour);
            return `about ${hours} hour${hours > 1 ? 's' : ''}`;
        } else if (diff < msPerMonth) {
            const days = Math.round(diff / msPerDay);
            return `about ${days} day${days > 1 ? 's' : ''}`;
        } else if (diff < msPerYear) {
            const months = Math.round(diff / msPerMonth);
            return `about ${months} month${months > 1 ? 's' : ''}`;
        } else {
            const years = Math.round(diff / msPerYear);
            return `about ${years} year${years > 1 ? 's' : ''}`;
        }
    }

    viewDetails(): void {
     this.router.navigate(['/repositories', this.repo.id]);
    }
}
