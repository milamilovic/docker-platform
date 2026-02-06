import { ChangeDetectorRef, Component } from '@angular/core';
import { Repository } from '../../shared/models/repository';
import { Page } from '../../shared/models/page';
import { RepositoryService } from './repository.service';

@Component({
  selector: 'app-home',
  standalone: false,
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  topPulled?: Repository[];
  topStorred?: Repository[];

  constructor(private repoService: RepositoryService, private cd: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadTopPulled(0, 8);
  }

  loadTopPulled(page: number, size: number) {
    this.repoService.getTopPulled(page, size).subscribe((data) => {
      this.topPulled = data.content;
      this.cd.markForCheck();
    });
  }

  loadTopStarred(page: number, size: number) {
    this.repoService.getTopPulled(page, size).subscribe((data) => {
      this.topStorred = data.content;
      this.cd.markForCheck();
    });
  }
}
